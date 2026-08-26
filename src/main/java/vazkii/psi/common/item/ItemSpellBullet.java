package vazkii.psi.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;

/** Basic spell bullet: a stackable spell container for the CAD magazine. */
public class ItemSpellBullet extends Item {

    private static final String TAG_SPELL = "spell";

    public ItemSpellBullet() {
        setMaxStackSize(16);
    }

    public static Spell getSpell(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey(TAG_SPELL) ? Spell.readFromNBT(
                    stack.getTagCompound()
                        .getCompoundTag(TAG_SPELL))
                    : null;
    }

    public static void setSpell(ItemStack stack, Spell spell) {
        if (stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = new NBTTagCompound();
        spell.writeToNBT(tag);
        stack.getTagCompound()
            .setTag(TAG_SPELL, tag);
    }

    /** Executes the normal (caster-focused) bullet immediately. */
    public void castSpell(ItemStack stack, EntityPlayer caster) throws Exception {
        Spell spell = getSpell(stack);
        if (spell == null) return;
        CompiledSpell compiled = new SpellCompiler().compile(spell);
        compiled.execute(
            new SpellContext().setPlayer(caster)
                .setSpell(spell));
    }

    /** Cost multiplier displayed by modern Psi. */
    public double getCostModifier() {
        return 1.0;
    }

    public String getBulletType() {
        return "basic";
    }

    /** Loopcast and circle bullets only work from a CAD magazine. */
    public boolean isCADOnlyContainer() {
        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Spell spell = getSpell(stack);
        return spell != null && spell.name != null && !spell.name.isEmpty() ? EnumChatFormatting.AQUA + spell.name
            : super.getItemStackDisplayName(stack);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add(
                EnumChatFormatting.GRAY + "Hold "
                    + EnumChatFormatting.AQUA
                    + "SHIFT"
                    + EnumChatFormatting.GRAY
                    + " for more info");
            return;
        }
        tooltip.add(EnumChatFormatting.AQUA + "Type" + EnumChatFormatting.GRAY + ": " + bulletTypeName());
        tooltip.add(
            EnumChatFormatting.AQUA + "Cost Multiplier"
                + EnumChatFormatting.GRAY
                + ": "
                + (int) (getCostModifier() * 100)
                + "%");
    }

    private String bulletTypeName() {
        String key = "psi.bullet_type_" + getBulletType();
        String translated = net.minecraft.util.StatCollector.translateToLocal(key);
        return key.equals(translated) ? getBulletType() : translated;
    }
}
