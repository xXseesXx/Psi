package vazkii.psi.api.cad;

import net.minecraft.item.ItemStack;

/**
 * An item that implements this works as a CAD colorizer, by which it can change
 * the CAD's spell color.
 */
public interface ICADColorizer {

    int DEFAULT_SPELL_COLOR = 0x13C5FF;

    /** Color of spells projected by a CAD that has this colorizer installed. */
    int getColor(ItemStack stack);
}
