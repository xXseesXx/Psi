package vazkii.psi.common.item.component;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.cad.EnumCADComponent;

/**
 * CAD socket component.
 */
public class ItemCADSocket extends ItemCADComponent {

    public static final int MAX_SOCKETS = 12;

    public ItemCADSocket() {
        super("SOCKET");
    }

    @Override
    public EnumCADComponent getComponentType(ItemStack stack) {
        return EnumCADComponent.SOCKET;
    }
}
