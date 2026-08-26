package vazkii.psi.common.item.component;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCADColorizerEmpty extends ItemCADColorizer {

    public ItemCADColorizerEmpty() {
        super(0x080808, false, "dye_cannister_inside");
    }

    @Override
    public int getColor(ItemStack stack) {
        return 0xFF080808;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        canisterIcon = register.registerIcon("psi:dye_cannister");
        overlayIcon = register.registerIcon("psi:dye_cannister_overlay");
        insideIcon = overlayIcon;
        itemIcon = canisterIcon;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
        return pass == 1 ? overlayIcon : canisterIcon;
    }
}
