package vazkii.psi.client.render.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.PieceTextureAtlas;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.spell.constant.PieceConstantNumber;

/** World-space 1.7.10 equivalent of Psi's Spell Programmer holographic canvas. */
public final class RenderTileProgrammer extends TileEntitySpecialRenderer {

    private static final ResourceLocation PROGRAMMER_TEXTURE = new ResourceLocation(
        "psi",
        "textures/gui/programmer.png");
    private static final float SCALE = 1F / 300F;
    private static final int PANEL_WIDTH = 174;
    private static final int PANEL_HEIGHT = 184;
    private static final int GRID_ORIGIN = 7;
    private static final int CELL_SIZE = 18;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        TileProgrammer programmer = (TileProgrammer) tile;
        if (!programmer.isEnabled()) return;

        // Bits 0-1 hold the horizontal facing; bit 3 is the enabled flag.
        int facing = programmer.getWorldObj()
            .getBlockMetadata(programmer.xCoord, programmer.yCoord, programmer.zCoord) & 3;
        // Set the canvas 5.2 model pixels in from the rear edge.
        double inset = 2.6D / 16D;
        double offsetX = facing == 1 ? -inset : facing == 3 ? inset : 0D;
        double offsetZ = facing == 0 ? inset : facing == 2 ? -inset : 0D;
        GL11.glPushMatrix();
        // The 184px projection is ~0.61 blocks tall at this scale. This
        // places its lower edge just above the 1-block-tall machine housing.
        GL11.glTranslated(x + .5D + offsetX, y + 1.35D, z + .5D + offsetZ);
        GL11.glRotatef(270F - facing * 90F, 0F, 1F, 0F);
        GL11.glRotatef(180F, 0F, 0F, 1F);
        GL11.glRotatef(-90F, 0F, 1F, 0F);
        GL11.glScalef(SCALE, SCALE, -SCALE);
        GL11.glTranslatef(-PANEL_WIDTH / 2F, -PANEL_HEIGHT / 2F, 0F);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1F, 1F, 1F, .5F);
        drawPanel();

        GL11.glColor4f(1F, 1F, 1F, 1F);
        drawPieces(programmer);
        drawConnections(programmer);
        drawName(programmer);

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void drawPanel() {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(PROGRAMMER_TEXTURE);
        float maxU = PANEL_WIDTH / 256F, maxV = PANEL_HEIGHT / 256F;
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(0, PANEL_HEIGHT, 0, 0, maxV);
        t.addVertexWithUV(PANEL_WIDTH, PANEL_HEIGHT, 0, maxU, maxV);
        t.addVertexWithUV(PANEL_WIDTH, 0, 0, maxU, 0);
        t.addVertexWithUV(0, 0, 0, 0, 0);
        t.draw();
    }

    private void drawPieces(TileProgrammer programmer) {
        for (int gridX = 0; gridX < 9; gridX++) for (int gridY = 0; gridY < 9; gridY++) {
            SpellPiece piece = programmer.spell.grid.gridData[gridX][gridY];
            if (piece == null) continue;
            int x = GRID_ORIGIN + gridX * CELL_SIZE;
            int y = GRID_ORIGIN + gridY * CELL_SIZE;
            PieceTextureAtlas.getInstance()
                .drawPiece(piece.registryKey.toString(), x, y);
            if (piece instanceof PieceConstantNumber) {
                String value = ((PieceConstantNumber) piece).getDisplayValue();
                if (value.length() > 5) value = value.substring(0, 5);
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(
                    value,
                    x + 9 - Minecraft.getMinecraft().fontRenderer.getStringWidth(value) / 2,
                    y + 5,
                    0xFFFFFF);
            }
        }
    }

    private void drawName(TileProgrammer programmer) {
        Minecraft.getMinecraft().fontRenderer.drawString("Name", GRID_ORIGIN, 164, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer
            .drawString(programmer.spell.name == null ? "" : programmer.spell.name, 38, 164, 0xFFFFFF);
    }

    private void drawConnections(TileProgrammer programmer) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(PROGRAMMER_TEXTURE);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int x = 0; x < 9; x++) for (int y = 0; y < 9; y++) {
            SpellPiece piece = programmer.spell.grid.gridData[x][y];
            if (piece == null) continue;
            for (java.util.Map.Entry<SpellParam<?>, SpellParam.Side> entry : piece.paramSides.entrySet()) {
                SpellParam.Side side = entry.getValue();
                if (!side.isEnabled()) continue;
                int count = countArrows(piece, side);
                int index = arrowIndex(piece, entry.getKey());
                SpellPiece neighbor = programmer.spell.grid.getPieceAtSideSafely(x, y, side);
                if (neighbor != null) {
                    int otherCount = countArrows(neighbor, side.getOpposite());
                    if (side.asInt() > side.getOpposite()
                        .asInt()) index += otherCount;
                    count += otherCount;
                }
                float percent = count > 1 ? (float) index / (count - 1) : .5F;
                addArrow(t, x, y, side, entry.getKey().color, percent);
            }
        }
        t.draw();
    }

    private int arrowIndex(SpellPiece piece, SpellParam<?> target) {
        int index = 0;
        for (SpellParam<?> parameter : piece.paramSides.keySet()) {
            if (parameter == target) return index;
            if (piece.paramSides.get(parameter) == piece.paramSides.get(target)) index++;
        }
        return index;
    }

    private int countArrows(SpellPiece piece, SpellParam.Side side) {
        int count = 0;
        for (SpellParam.Side current : piece.paramSides.values()) if (current == side) count++;
        return count;
    }

    private void addArrow(Tessellator t, int gridX, int gridY, SpellParam.Side side, int color, float percent) {
        float minX = GRID_ORIGIN + gridX * CELL_SIZE + 4 + side.minx * percent + side.maxx * (1 - percent);
        float minY = GRID_ORIGIN + gridY * CELL_SIZE + 4 + side.miny * percent + side.maxy * (1 - percent);
        float maxX = minX + 8, maxY = minY + 8;
        float minU = side.u / 256F, minV = side.v / 256F, maxU = (side.u + 8) / 256F, maxV = (side.v + 8) / 256F;
        t.setColorRGBA((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
        t.addVertexWithUV(minX, maxY, 0, minU, maxV);
        t.addVertexWithUV(maxX, maxY, 0, maxU, maxV);
        t.addVertexWithUV(maxX, minY, 0, maxU, minV);
        t.addVertexWithUV(minX, minY, 0, minU, minV);
    }
}
