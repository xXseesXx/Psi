package vazkii.psi.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import vazkii.psi.client.core.handler.KeybindHandler;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.network.PacketCADSelect;
import vazkii.psi.common.network.PacketHandler;

/** 1.7 port of Psi's original hold-to-select socket radial. */
public class GuiCADSelect extends GuiScreen {
    private static final ResourceLocation[] SIGNS = new ResourceLocation[ItemCreativeCAD.MAGAZINE_SIZE];
    static { for (int i = 0; i < SIGNS.length; i++) SIGNS[i] = new ResourceLocation("psi", "textures/gui/signs/sign" + i + ".png"); }
    private final ItemStack cad;
    private int timeIn, slotSelected = -1;

    public GuiCADSelect(ItemStack cad) { this.cad = cad; }

    @Override public void drawScreen(int mx, int my, float partial) {
        int x = width / 2, y = height / 2, maxRadius = 80, segments = ItemCreativeCAD.MAGAZINE_SIZE;
        double angle = (Math.atan2(my - y, mx - x) + Math.PI * 2) % (Math.PI * 2);
        float step = (float) Math.PI / 180F, degPer = (float) Math.PI * 2F / segments;
        slotSelected = -1;
        Tessellator tess = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_DEPTH_TEST); GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); GL11.glShadeModel(GL11.GL_SMOOTH);
        for (int seg = 0; seg < segments; seg++) {
            boolean hover = degPer * seg < angle && angle < degPer * (seg + 1);
            float radius = Math.max(0, Math.min((timeIn + partial - seg * 6F / segments) * 40F, maxRadius));
            if (hover) radius *= 1.025F;
            int gray = 0x40 + (seg % 2 == 0 ? 0x19 : 0), r = gray, g = gray, b = gray;
            if (hover) { slotSelected = seg; r = 79; g = 239; b = 255; }
            tess.startDrawing(GL11.GL_TRIANGLE_FAN); tess.setColorRGBA(r, g, b, 0x66); tess.addVertex(x, y, 0);
            for (float i = 0; i < degPer + step / 2; i += step) { float rad = i + seg * degPer; tess.addVertex(x + MathHelper.cos(rad) * radius, y + MathHelper.sin(rad) * radius, 0); }
            tess.draw();
        }
        GL11.glShadeModel(GL11.GL_FLAT); GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glColor4f(1F, 1F, 1F, 1F);
        for (int seg = 0; seg < segments; seg++) {
            boolean hover = degPer * seg < angle && angle < degPer * (seg + 1);
            float radius = Math.max(0, Math.min((timeIn + partial - seg * 6F / segments) * 40F, maxRadius));
            if (hover || seg == ItemCreativeCAD.getSelectedSlot(cad)) radius *= 1.025F;
            float rad = (seg + .5F) * degPer, xp = x + MathHelper.cos(rad) * radius, yp = y + MathHelper.sin(rad) * radius;
            ItemStack bullet = ItemCreativeCAD.getBullet(cad, seg);
            if (bullet == null) continue;
            itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), bullet, (int) ((xp - x) * .6 + x) - 8, (int) ((yp - y) * .6 + y) - 8);
            String name = (hover ? EnumChatFormatting.UNDERLINE.toString() : "") + bullet.getDisplayName(); int tw = fontRendererObj.getStringWidth(name);
            float tx = xp - 4, ty = yp; if (tx < x) tx -= tw - 8; if (ty < y) ty -= 9;
            fontRendererObj.drawStringWithShadow(name, (int) tx, (int) ty, 0xFFFFFF);
            if (seg == ItemCreativeCAD.getSelectedSlot(cad)) fontRendererObj.drawStringWithShadow("Selected", (int) (tx + tw / 4F), (int) (ty + 9), 0x00FF00);
            mc.getTextureManager().bindTexture(SIGNS[seg]); drawTexturedModalRect((int) ((xp - x) * .8 + x) - 8, (int) ((yp - y) * .8 + y) - 8, 0, 0, 16, 16);
        }
        float scale = 3 * Math.min(5, timeIn + partial) / 5F;
        GL11.glPushMatrix(); GL11.glScalef(scale, scale, scale); itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), cad, (int) (x / scale) - 8, (int) (y / scale) - 8); GL11.glPopMatrix(); GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
    @Override public void updateScreen() { if (!GameSettings.isKeyDown(KeybindHandler.PSI_MASTER)) { mc.displayGuiScreen(null); if (slotSelected != -1) { ItemCreativeCAD.setSelectedSlot(cad, slotSelected); PacketHandler.INSTANCE.sendToServer(new PacketCADSelect(slotSelected)); } } timeIn++; }
    @Override public boolean doesGuiPauseGame() { return false; }
}
