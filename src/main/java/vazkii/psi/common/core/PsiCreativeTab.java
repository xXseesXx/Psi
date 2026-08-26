package vazkii.psi.common.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import vazkii.psi.common.core.proxy.CommonProxy;

/** The dedicated creative-inventory tab for Psi content. */
public final class PsiCreativeTab {

    public static final CreativeTabs TAB = new CreativeTabs("psi") {

        @Override
        public Item getTabIconItem() {
            return CommonProxy.itemCADAssemblyIron != null ? CommonProxy.itemCADAssemblyIron : CommonProxy.itemCAD;
        }
    };

    private PsiCreativeTab() {}
}
