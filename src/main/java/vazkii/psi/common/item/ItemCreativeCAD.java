package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.entity.EntitySpellProjectile;

/** Creative CAD with a twelve-round spell-bullet magazine. */
public class ItemCreativeCAD extends Item {
    public static final int MAGAZINE_SIZE=12;
    public ItemCreativeCAD() { setMaxStackSize(1); }
    public static ItemStack getBullet(ItemStack cad,int slot) { if(cad==null||!cad.hasTagCompound()) return null; NBTTagList list=cad.getTagCompound().getTagList("bullets",10); for(int i=0;i<list.tagCount();i++){NBTTagCompound tag=list.getCompoundTagAt(i);if((tag.getByte("Slot")&255)==slot)return ItemStack.loadItemStackFromNBT(tag);} return null; }
    public static void setBullet(ItemStack cad,int slot,ItemStack bullet) { if(cad.getTagCompound()==null) cad.setTagCompound(new NBTTagCompound()); NBTTagList out=new NBTTagList(); for(int i=0;i<MAGAZINE_SIZE;i++){ItemStack current=i==slot?bullet:getBullet(cad,i);if(current!=null){NBTTagCompound tag=new NBTTagCompound();tag.setByte("Slot",(byte)i);current.writeToNBT(tag);out.appendTag(tag);}} cad.getTagCompound().setTag("bullets",out); }
    public static int getSelectedSlot(ItemStack cad) { return cad.hasTagCompound() ? Math.max(0,Math.min(MAGAZINE_SIZE-1,cad.getTagCompound().getInteger("selectedSlot"))) : 0; }
    public static void setSelectedSlot(ItemStack cad,int slot) { if(cad.getTagCompound()==null)cad.setTagCompound(new NBTTagCompound());cad.getTagCompound().setInteger("selectedSlot",Math.max(0,Math.min(MAGAZINE_SIZE-1,slot))); }
    @Override public ItemStack onItemRightClick(ItemStack cad,World world,EntityPlayer player) { if(world.isRemote)return cad; ItemStack bullet=getBullet(cad,getSelectedSlot(cad));Spell spell=ItemSpellBullet.getSpell(bullet);if(spell!=null){world.spawnEntityInWorld(new EntitySpellProjectile(world,player,spell));return cad;} player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+"[Psi] No programmed spell bullet selected"));return cad; }
}
