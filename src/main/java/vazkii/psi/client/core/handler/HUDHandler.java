package vazkii.psi.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.item.base.IHUDItem;

/** 1.7 renderer for Psi's vertical resource bar. Ported from the 1.21.1 HUDHandler. */
public class HUDHandler {

    private static final ResourceLocation BAR = new ResourceLocation("psi", "textures/gui/psi_bar.png");

    /** Number of ticks the lost Psi remains visible as a ghost. */
    private static final int MAX_GHOST_TICKS = 30;

    // TODO 1.21.1: these come from PlayerDataHandler.PlayerData (getTotalPsi/getAvailablePsi),
    // with data.deductions driving the shatter/percentile bar-chunk animation and
    // data.isOverflowed() swapping the fill to red. None of that exists on this port yet,
    // so these two fixed-value statics stand in for both totalPsi and availablePsi.
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
        if (mc.thePlayer == null) return;

        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        net.minecraft.item.ItemStack held = mc.thePlayer.getHeldItem();

        // 1.21.1's HUD_ITEM is its own render layer, independent of the psi bar - so this
        // fires for any held item implementing IHUDItem before the CAD check below can bail.
        if (held != null && held.getItem() instanceof IHUDItem) {
            ((IHUDItem) held.getItem())
                .drawHUD(event.partialTicks, resolution.getScaledWidth(), resolution.getScaledHeight(), held);
        }

        if (held == null || !(held.getItem() instanceof ICAD)) {
            return;
        }

        ICAD cad = (ICAD) held.getItem();
        int psiColor = cad.getSpellColor(held);

        // TODO 1.21.1: also skipped drawing entirely here if ConfigHandler.CLIENT.contextSensitiveBar
        // was set and the bar was already full and no held/offhand item wanted it shown
        // (IPsiBarDisplay). No capability system on 1.7.10 and no config for it yet, so the
        // bar always draws whenever a CAD is held.

        int pad = 3;
        int width = 32;
        int height = 140;

        // TODO 1.21.1: ConfigHandler.CLIENT.psiBarOnRight picks the side; no config yet, hardcoded right.
        // Modern Psi intentionally lets the frame overlap the screen edge by `pad` pixels.
        int x = resolution.getScaledWidth() - width + pad;
        int y = resolution.getScaledHeight() / 2 - height / 2;

        mc.getTextureManager()
            .bindTexture(BAR);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        net.minecraft.client.gui.Gui.func_146110_a(x, y, 0, 0, width, height, 64, 256);

        x += 8;
        y += 26;

        width = 16;
        height = 106;

        int origHeight = height;
        int origY = y;

        float r = 0.6F;
        float g = 0.65F;
        float b = 1F;
        // TODO 1.21.1: r/g/b flipped to (1F, 0.6F, 0.6F) when data.isOverflowed(). No overflow
        // state on this port yet, so the fill is always the default blue.

        int fill = (int) (origHeight * (double) current / maximum);
        int fillOffset = origHeight - fill;

        // Main Psi fill.
        GL11.glColor4f(r, g, b, 1F);

        net.minecraft.client.gui.Gui.func_146110_a(x, origY + fillOffset, 32, fillOffset, width, fill, 64, 256);

        /*
         * Ghost Psi effect.
         * When Psi is spent, each ghost stores the previous amount and remains
         * visible above the current fill while fading out. 1.21.1 gets a comparable
         * look from data.deductions plus a shatter shader (usePsiBarShader); there's
         * no programmable GUI shader pipeline on 1.7.10, so this stays a plain
         * alpha-faded textured quad per ghost instead - same visual idea, cheaper effect.
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

                int ghostFill = (int) (origHeight * (double) ghost.psi / maximum);
                int ghostOffset = origHeight - ghostFill;

                int lost = ghostFill - fill;

                if (lost > 0) {
                    float alpha = Math.min(1F, remainingTicks / (float) MAX_GHOST_TICKS);

                    GL11.glColor4f(r, g, b, alpha);

                    net.minecraft.client.gui.Gui
                        .func_146110_a(x, origY + ghostOffset, 32, ghostOffset, width, lost, 64, 256);
                }
            }
        }

        /*
         * Horizontal line underneath the current Psi value.
         * Moved 12 px to the right and extended by 12 px,
         * resulting in the right edge being 24 px farther right.
         */
        float red = ((psiColor >> 16) & 255) / 255F;
        float green = ((psiColor >> 8) & 255) / 255F;
        float blue = (psiColor & 255) / 255F;
        GL11.glColor4f(red, green, blue, 1.0F);

        net.minecraft.client.gui.Gui.func_146110_a(x - 10, origY + fillOffset - 2, 0, 140, 56, 3, 64, 256);

        // Current Psi value.
        String value = Integer.toString(current);

        mc.fontRenderer.drawStringWithShadow(
            value,
            x + 1 - mc.fontRenderer.getStringWidth(value),
            origY + fillOffset - 11,
            psiColor);

        // Stored (overflow) Psi value. -1 means an infinite reserve (e.g. creative CAD) -> "∞",
        // matching the 1.21.1 convention, instead of only ever printing this for ItemCAD.
        int storedPsi = cad.getStoredPsi(held);
        String overflow = storedPsi == -1 ? "\u221E" : Integer.toString(storedPsi);

        /*
         * Bottom text normally sits at y + 126.
         * If the top text/line moves down far enough, push the
         * bottom text down so the gap stays at approximately
         * 10 screen pixels at GUI Scale 3.
         */
        int bottomTextY = origY - 26 + 126;

        int lineY = origY + fillOffset - 2;
        int lineBottomY = lineY + 3;

        // 2 GUI pixels ≈ 10 screen pixels at GUI Scale 3.
        int minGap = 2;

        int minimumBottomY = lineBottomY + minGap;

        if (bottomTextY < minimumBottomY) {
            bottomTextY = minimumBottomY;
        }

        mc.fontRenderer
            .drawStringWithShadow(overflow, x + 1 - mc.fontRenderer.getStringWidth(overflow), bottomTextY, psiColor);

        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
