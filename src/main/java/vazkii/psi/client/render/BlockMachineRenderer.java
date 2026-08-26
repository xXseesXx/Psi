package vazkii.psi.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import vazkii.psi.common.block.BlockCADAssembler;
import vazkii.psi.common.block.BlockProgrammer;

/**
 * 1.7.10 equivalent of the modern CAD assembler/programmer JSON models.
 * The spell programmer's changing spell-grid display is intentionally not rendered.
 */
public final class BlockMachineRenderer implements ISimpleBlockRenderingHandler {

    private static final double U = 1D / 16D;
    // [support (NW, NE, SE, SW)][face (N, E, S, W)][u1, v1, u2, v2]
    private static final int[][][] SUPPORT_UVS = {
        { { 4, 7, 0, 11 }, { 7, 7, 3, 11 }, { 3, 7, 7, 11 }, { 0, 7, 4, 11 } },
        { { 0, 7, 4, 11 }, { 4, 7, 0, 11 }, { 7, 7, 3, 11 }, { 3, 7, 7, 11 } },
        { { 3, 7, 7, 11 }, { 0, 7, 4, 11 }, { 4, 7, 0, 11 }, { 7, 7, 3, 11 } },
        { { 7, 7, 3, 11 }, { 3, 7, 7, 11 }, { 0, 7, 4, 11 }, { 4, 7, 0, 11 } } };
    private final int renderId;

    public BlockMachineRenderer(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        renderPart(block, renderer, 0, 9 * U, 0, 1, 1, 1, metadata);
        setRenderingBase(block, true);
        renderPart(block, renderer, U, 0, U, 15 * U, 5 * U, 15 * U, metadata);
        setRenderingBase(block, false);
        setRenderingBase(block, true);
        renderSupports(block, renderer, metadata);
        setRenderingBase(block, false);
        renderer.setRenderBoundsFromBlock(block);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (!(block instanceof BlockProgrammer) && !(block instanceof BlockCADAssembler)) return false;
        int facing = world.getBlockMetadata(x, y, z) & 3;
        renderer.uvRotateTop = facing;
        renderer.uvRotateBottom = facing;

        if (block instanceof BlockProgrammer) renderProgrammerHousing(world, (BlockProgrammer) block, x, y, z, facing);
        else {
            renderer.setRenderBounds(0, 9 * U, 0, 1, 1, 1);
            renderer.renderStandardBlock(block, x, y, z);
        }
        renderer.setRenderBounds(U, 0, U, 15 * U, 5 * U, 15 * U);
        setRenderingBase(block, true);
        renderer.renderStandardBlock(block, x, y, z);
        setRenderingBase(block, false);
        setRenderingBase(block, true);
        renderSupports(world, block, x, y, z, facing);
        setRenderingBase(block, false);
        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;
        renderer.setRenderBoundsFromBlock(block);
        return true;
    }

    private void renderSupports(IBlockAccess world, Block block, int x, int y, int z, int facing) {
        int[][] positions = { { 2, 2 }, { 10, 2 }, { 10, 10 }, { 2, 10 } };
        for (int target = 0; target < 4; target++) {
            int source = (target - facing + 4) & 3;
            int[][] uv = new int[4][];
            for (int face = 0; face < 4; face++) uv[face] = SUPPORT_UVS[source][(face + facing) & 3];
            renderSupport(world, block, x, y, z, positions[target][0], positions[target][1], uv);
        }
    }

    /** Renders the four explicitly mapped side faces from the Blockbench model. */
    private void renderSupport(IBlockAccess world, Block block, int x, int y, int z, int modelX, int modelZ,
        int[][] uv) {
        double minX = x + modelX * U, maxX = minX + 4 * U;
        double minY = y + 5 * U, maxY = y + 9 * U;
        double minZ = z + modelZ * U, maxZ = minZ + 4 * U;
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        IIcon icon = block.getIcon(2, 0);
        faceZNeg(t, icon, minX, minY, minZ, maxX, maxY, uv[0]);
        faceXPos(t, icon, maxX, minY, minZ, maxY, maxZ, uv[1]);
        faceZPos(t, icon, minX, minY, maxZ, maxX, maxY, uv[2]);
        faceXNeg(t, icon, minX, minY, minZ, maxY, maxZ, uv[3]);
    }

    /** The programmer's upper shell is asymmetric: only its facing side has buttons. */
    private void renderProgrammerHousing(IBlockAccess world, BlockProgrammer block, int x, int y, int z, int facing) {
        double minY = y + 9 * U, maxY = y + 1D;
        Tessellator t = Tessellator.instance;
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        IIcon side = block.getIcon(2, world.getBlockMetadata(x, y, z));
        IIcon top = block.getIcon(1, world.getBlockMetadata(x, y, z));
        IIcon bottom = block.getIcon(0, world.getBlockMetadata(x, y, z));
        // Placement metadata points toward the player; the original model's
        // button face therefore uses this direction, not its opposite.
        int front = facing;
        for (int face = 0; face < 4; face++) {
            int[] uv = face == front ? new int[] { 0, 0, 16, 7 } : new int[] { 0, 7, 16, 14 };
            if (face == 0) faceZNeg(t, side, x, minY, z, x + 1D, maxY, uv);
            else if (face == 1) faceXPos(t, side, x + 1D, minY, z, maxY, z + 1D, uv);
            else if (face == 2) faceZPos(t, side, x, minY, z + 1D, x + 1D, maxY, uv);
            else faceXNeg(t, side, x, minY, z, maxY, z + 1D, uv);
        }
        faceTop(t, top, x, maxY, z, facing);
        faceBottom(t, bottom, x, minY, z);
    }

    private void faceTop(Tessellator t, IIcon i, double x, double y, double z, int facing) {
        double[] u = { i.getInterpolatedU(0), i.getInterpolatedU(16), i.getInterpolatedU(16), i.getInterpolatedU(0) };
        double[] v = { i.getInterpolatedV(0), i.getInterpolatedV(0), i.getInterpolatedV(16), i.getInterpolatedV(16) };

        t.setColorOpaque_F(1F, 1F, 1F);

        t.addVertexWithUV(x + 1D, y, z + 1D, u[(4 - facing) & 3], v[(4 - facing) & 3]);
        t.addVertexWithUV(x + 1D, y, z, u[(3 - facing + 4) & 3], v[(3 - facing + 4) & 3]);
        t.addVertexWithUV(x, y, z, u[(2 - facing + 4) & 3], v[(2 - facing + 4) & 3]);
        t.addVertexWithUV(x, y, z + 1D, u[(5 - facing) & 3], v[(5 - facing) & 3]);
    }

    private void faceBottom(Tessellator t, IIcon i, double x, double y, double z) {
        t.setColorOpaque_F(.5F, .5F, .5F);
        t.addVertexWithUV(x, y, z, i.getInterpolatedU(0), i.getInterpolatedV(0));
        t.addVertexWithUV(x + 1D, y, z, i.getInterpolatedU(16), i.getInterpolatedV(0));
        t.addVertexWithUV(x + 1D, y, z + 1D, i.getInterpolatedU(16), i.getInterpolatedV(16));
        t.addVertexWithUV(x, y, z + 1D, i.getInterpolatedU(0), i.getInterpolatedV(16));
    }

    private void faceZNeg(Tessellator t, IIcon i, double minX, double minY, double z, double maxX, double maxY,
        int[] uv) {
        face(t, i, minX, maxY, z, maxX, maxY, z, maxX, minY, z, minX, minY, z, uv, .8F);
    }

    private void faceXPos(Tessellator t, IIcon i, double x, double minY, double minZ, double maxY, double maxZ,
        int[] uv) {
        face(t, i, x, maxY, minZ, x, maxY, maxZ, x, minY, maxZ, x, minY, minZ, uv, .6F);
    }

    private void faceZPos(Tessellator t, IIcon i, double minX, double minY, double z, double maxX, double maxY,
        int[] uv) {
        face(t, i, maxX, maxY, z, minX, maxY, z, minX, minY, z, maxX, minY, z, uv, .8F);
    }

    private void faceXNeg(Tessellator t, IIcon i, double x, double minY, double minZ, double maxY, double maxZ,
        int[] uv) {
        face(t, i, x, maxY, maxZ, x, maxY, minZ, x, minY, minZ, x, minY, maxZ, uv, .6F);
    }

    private void face(Tessellator t, IIcon i, double x1, double y1, double z1, double x2, double y2, double z2,
        double x3, double y3, double z3, double x4, double y4, double z4, int[] uv, float shade) {
        double u1 = i.getInterpolatedU(uv[0]), v1 = i.getInterpolatedV(uv[1]), u2 = i.getInterpolatedU(uv[2]),
            v2 = i.getInterpolatedV(uv[3]);
        t.setColorOpaque_F(shade, shade, shade);
        t.addVertexWithUV(x1, y1, z1, u2, v1);
        t.addVertexWithUV(x2, y2, z2, u1, v1);
        t.addVertexWithUV(x3, y3, z3, u1, v2);
        t.addVertexWithUV(x4, y4, z4, u2, v2);
    }

    private void setRenderingBase(Block block, boolean renderingBase) {
        if (block instanceof BlockProgrammer) ((BlockProgrammer) block).setRenderingBase(renderingBase);
        else if (block instanceof BlockCADAssembler) ((BlockCADAssembler) block).setRenderingBase(renderingBase);
    }

    private void renderSupports(Block block, RenderBlocks renderer, int metadata) {
        renderPart(block, renderer, 2 * U, 5 * U, 2 * U, 6 * U, 9 * U, 6 * U, metadata);
        renderPart(block, renderer, 10 * U, 5 * U, 2 * U, 14 * U, 9 * U, 6 * U, metadata);
        renderPart(block, renderer, 10 * U, 5 * U, 10 * U, 14 * U, 9 * U, 14 * U, metadata);
        renderPart(block, renderer, 2 * U, 5 * U, 10 * U, 6 * U, 9 * U, 14 * U, metadata);
    }

    private void renderPart(Block block, RenderBlocks renderer, int x, int y, int z, double minX, double minY,
        double minZ, double maxX, double maxY, double maxZ) {
        renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        renderer.renderStandardBlock(block, x, y, z);
    }

    private void renderPart(Block block, RenderBlocks renderer, double minX, double minY, double minZ, double maxX,
        double maxY, double maxZ, int metadata) {
        renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0, -1, 0);
        renderer.renderFaceYNeg(block, -0.5D, -0.5D, -0.5D, block.getIcon(0, metadata));
        tessellator.setNormal(0, 1, 0);
        renderer.renderFaceYPos(block, -0.5D, -0.5D, -0.5D, block.getIcon(1, metadata));
        tessellator.setNormal(0, 0, -1);
        renderer.renderFaceZNeg(block, -0.5D, -0.5D, -0.5D, block.getIcon(2, metadata));
        tessellator.setNormal(0, 0, 1);
        renderer.renderFaceZPos(block, -0.5D, -0.5D, -0.5D, block.getIcon(3, metadata));
        tessellator.setNormal(-1, 0, 0);
        renderer.renderFaceXNeg(block, -0.5D, -0.5D, -0.5D, block.getIcon(4, metadata));
        tessellator.setNormal(1, 0, 0);
        renderer.renderFaceXPos(block, -0.5D, -0.5D, -0.5D, block.getIcon(5, metadata));
        tessellator.draw();
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
