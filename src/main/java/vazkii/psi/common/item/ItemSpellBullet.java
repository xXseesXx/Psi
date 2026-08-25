package vazkii.psi.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import vazkii.psi.api.spell.Spell;

/** Basic spell bullet: a stackable spell container for the CAD magazine. */
public class ItemSpellBullet extends Item {
    private static final String TAG_SPELL="spell";
    public ItemSpellBullet() { setMaxStackSize(16); }
    public static Spell getSpell(ItemStack stack) { return stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_SPELL) ? Spell.readFromNBT(stack.getTagCompound().getCompoundTag(TAG_SPELL)) : null; }
    public static void setSpell(ItemStack stack, Spell spell) { if(stack.getTagCompound()==null) stack.setTagCompound(new NBTTagCompound()); NBTTagCompound tag=new NBTTagCompound(); spell.writeToNBT(tag); stack.getTagCompound().setTag(TAG_SPELL,tag); }
    @Override public String getItemStackDisplayName(ItemStack stack) {
        Spell spell = getSpell(stack);
        return spell != null && spell.name != null && !spell.name.isEmpty() ? EnumChatFormatting.AQUA + spell.name : super.getItemStackDisplayName(stack);
    }
}
