package vazkii.psi.common.item.component;

import java.awt.Color;

import net.minecraft.item.ItemStack;

import vazkii.psi.common.Psi;

public class ItemCADColorizerRainbow extends ItemCADColorizer {

    public ItemCADColorizerRainbow() {
        super(0xFFFFFF, false, "dye_cannister_special_rainbow");
    }

    @Override
    public int getColor(ItemStack stack) {
        return Color.HSBtoRGB(Psi.proxy.getFrameTicks() * 0.005F, 1F, 1F);
    }
}
