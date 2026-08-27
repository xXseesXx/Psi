package vazkii.psi.api.cad;

import net.minecraft.item.ItemStack;

/**
 * An item that implements this counts as a CAD component and can be used to
 * create a CAD.
 */
public interface ICADComponent {
    /** Gets the component type of the given stack. */
    EnumCADComponent getComponentType(ItemStack stack);

    /** Gets the stat value for the respective stat of the stack. */
    int getCADStatValue(ItemStack stack, EnumCADStat stat);
}
