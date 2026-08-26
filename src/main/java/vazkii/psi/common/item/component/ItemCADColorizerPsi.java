package vazkii.psi.common.item.component;

import java.awt.Color;

import net.minecraft.item.ItemStack;

import vazkii.psi.common.Psi;

public class ItemCADColorizerPsi extends ItemCADColorizer {

    public ItemCADColorizerPsi() {
        super(0x13C5FF, false, "dye_cannister_special_psi");
    }

    @Override
    public int getColor(ItemStack stack) {
        float time = Psi.proxy.getFrameTicks();
        float w = (float) (Math.sin(time * 0.4) * 0.5 + 0.5) * 0.1F;
        float r = (float) (Math.sin(time * 0.1) * 0.5 + 0.5) * 0.5F + 0.25F + w;
        float g = 0.5F + w;
        float b = 1F;
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255)).getRGB();
    }
}
