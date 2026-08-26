package vazkii.psi.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;

/** 1.7 renderer for Psi's vertical resource bar. */
public class PsiHUDHandler {

    private static final ResourceLocation BAR = new ResourceLocation("psi", "textures/gui/psi_bar.png");

    /** Number of ticks the lost Psi remains visible as a ghost. */
    private static final int MAX_GHOST_TICKS = 30;

    private static int current = 5000, maximum = 5000;

    private static class PsiGhost {

        int psi;
        long endTick;

        PsiGhost(int psi, long endTick) {
            this.psi = psi;
            this.endTick = endTick;
        }
    }

    private static final java.util.List<PsiGhost> ghosts = new java.util.ArrayList<PsiGhost>();

    public static void setPsi(int previous, int value, int max) {
        maximum = Math.max(1, max);

        if (previous > value && Minecraft.getMinecraft().theWorld != null) {
            ghosts.add(new PsiGhost(previous, Minecraft.getMinecraft().theWorld.getTotalWorldTime() + MAX_GHOST_TICKS));
        }

        current = value;
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();

        net.minecraft.item.ItemStack held = mc.thePlayer == null ? null : mc.thePlayer.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemCAD || held.getItem() instanceof ItemCreativeCAD)) {
            return;
        }
        int psiColor = held.getItem() instanceof ItemCAD ? ItemCAD.getSpellColor(held)
            : ICADColorizer.DEFAULT_SPELL_COLOR;

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        // Modern Psi intentionally lets the frame overlap the screen edge by three pixels.
        int x = resolution.getScaledWidth() - 29;
        int y = resolution.getScaledHeight() / 2 - 70;

        mc.getTextureManager()
            .bindTexture(BAR);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        net.minecraft.client.gui.Gui.func_146110_a(x, y, 0, 0, 32, 140, 64, 256);

        int fill = (int) (106D * current / maximum);
        int fillOffset = 106 - fill;

        // Main Psi fill.
        GL11.glColor4f(0.6F, 0.65F, 1F, 1F);

        net.minecraft.client.gui.Gui.func_146110_a(x + 8, y + 26 + fillOffset, 32, fillOffset, 16, fill, 64, 256);

        /*
         * Ghost Psi effect.
         * When Psi is spent, ghostPsi stores the previous amount and
         * remains visible above the current fill while fading out.
         */
        if (mc.theWorld != null && !ghosts.isEmpty()) {
            long currentTick = mc.theWorld.getTotalWorldTime();

            for (int i = ghosts.size() - 1; i >= 0; i--) {
                PsiGhost ghost = ghosts.get(i);

                long remainingTicks = ghost.endTick - currentTick;

                if (remainingTicks <= 0 || ghost.psi <= current) {
                    ghosts.remove(i);
                    continue;
                }

                int ghostFill = (int) (106D * ghost.psi / maximum);
                int ghostOffset = 106 - ghostFill;

                int lost = ghostFill - fill;

                if (lost > 0) {
                    float alpha = Math.min(1F, remainingTicks / (float) MAX_GHOST_TICKS);

                    GL11.glColor4f(0.6F, 0.65F, 1F, alpha);

                    net.minecraft.client.gui.Gui
                        .func_146110_a(x + 8, y + 26 + ghostOffset, 32, ghostOffset, 16, lost, 64, 256);
                }
            }
        }

        /*
         * Horizontal line underneath the current Psi value.
         * Moved 12 px to the right and extended by 12 px,
         * resulting in the right edge being 24 px farther right.
         */
        // GL11.glColor4f(0x13 / 255F, 0xC5 / 255F, 1F, 1F);
        float red = ((psiColor >> 16) & 255) / 255F;
        float green = ((psiColor >> 8) & 255) / 255F;
        float blue = (psiColor & 255) / 255F;
        GL11.glColor4f(red, green, blue, 1.0F);

        net.minecraft.client.gui.Gui.func_146110_a(x - 10, y + 26 + fillOffset - 2, 0, 140, 56, 3, 64, 256);

        // Current Psi value.
        String value = Integer.toString(current);

        mc.fontRenderer.drawStringWithShadow(
            value,
            x + 1 - mc.fontRenderer.getStringWidth(value),
            y + 26 + fillOffset - 11,
            psiColor);

        ItemCAD cad = held.getItem() instanceof ItemCAD ? (ItemCAD) held.getItem() : null;

        if (cad != null) {
            String overflow = Integer.toString(ItemCAD.getStoredPsi(held));

            /*
             * Bottom text normally sits at y + 126.
             * If the top text/line moves down far enough, push the
             * bottom text down so the gap stays at approximately
             * 10 screen pixels at GUI Scale 3.
             */
            int bottomTextY = y + 126;

            int lineY = y + 26 + fillOffset - 2;
            int lineBottomY = lineY + 3;

            // 2 GUI pixels ≈ 10 screen pixels at GUI Scale 3.
            int minGap = 2;

            int minimumBottomY = lineBottomY + minGap;

            if (bottomTextY < minimumBottomY) {
                bottomTextY = minimumBottomY;
            }

            mc.fontRenderer.drawStringWithShadow(
                overflow,
                x + 1 - mc.fontRenderer.getStringWidth(overflow),
                bottomTextY,
                psiColor);
        }

        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
