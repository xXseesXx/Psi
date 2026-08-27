package vazkii.psi.common.item.base;

import net.minecraft.item.ItemStack;

/**
 * 1.7.10 port of the 1.21.1 IHUDItem hook.
 *
 * The 1.21.1 version receives a GuiGraphics + DeltaTracker; 1.7.10 has neither,
 * so implementations get the same raw parameters HUDHandler itself uses
 * (partial ticks, scaled screen size) and are expected to issue their own
 * GL11 / Gui.func_146110_a calls, same as HUDHandler's psi bar rendering does.
 */
public interface IHUDItem {

    void drawHUD(float partialTicks, int screenWidth, int screenHeight, ItemStack stack);
}
