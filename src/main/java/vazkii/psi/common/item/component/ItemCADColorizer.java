package vazkii.psi.common.item.component;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import vazkii.psi.api.cad.ICADColorizer;

/** Dye CAD colorizer: a tinted canister whose color is applied to spells. */
public class ItemCADColorizer extends ItemCADComponent implements ICADColorizer {

    public static final String[] DYE_NAMES = { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
        "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black" };
    /** DyeColor.getTextColor() values from modern Minecraft. */
    public static final int[] DYE_COLORS = { 0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F, 0xF38BAA,
        0x474F52, 0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA, 0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21 };

    private final int color;
    private final boolean tintInside;
    private final String insideTexture;
    protected IIcon canisterIcon, insideIcon, overlayIcon;

    public ItemCADColorizer(int color) {
        this(color, true, "dye_cannister_inside");
    }

    protected ItemCADColorizer(int color, boolean tintInside, String insideTexture) {
        super("Colorizer");
        this.color = color;
        this.tintInside = tintInside;
        this.insideTexture = insideTexture;
    }

    @Override
    public int getColor(ItemStack stack) {
        return color | 0xFF000000;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        canisterIcon = register.registerIcon("psi:dye_cannister");
        insideIcon = register.registerIcon("psi:" + insideTexture);
        overlayIcon = register.registerIcon("psi:dye_cannister_overlay");
        itemIcon = canisterIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
        if (pass == 1) return insideIcon;
        if (pass == 2) return overlayIcon;
        return canisterIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        return pass == 1 && tintInside ? getColor(stack) : 0xFFFFFF;
    }

    public static int colorOf(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ICADColorizer)
            return ((ICADColorizer) stack.getItem()).getColor(stack);
        return ICADColorizer.DEFAULT_SPELL_COLOR;
    }
}
