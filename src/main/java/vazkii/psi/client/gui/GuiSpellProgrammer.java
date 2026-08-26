package vazkii.psi.client.gui;

import net.minecraft.item.ItemStack;

import vazkii.psi.common.block.tile.TileProgrammer;

/** Compatibility entry point used by the 1.7.10 GUI handler and addons. */
public class GuiSpellProgrammer extends GuiProgrammer {

    public GuiSpellProgrammer(ItemStack cadStack) {
        super(cadStack);
    }

    public GuiSpellProgrammer(TileProgrammer programmer) {
        super(programmer);
    }
}
