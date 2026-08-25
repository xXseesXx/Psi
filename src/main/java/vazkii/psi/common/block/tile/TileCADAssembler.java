package vazkii.psi.common.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemSpellBullet;

/** Inventory backing the assembler's future CAD magazine. CAD construction is intentionally not implemented here. */
public class TileCADAssembler extends TileEntity implements IInventory {
    public static final int MAGAZINE_SLOTS = 12;
    private final ItemStack[] inventory = new ItemStack[MAGAZINE_SLOTS + 1];
    @Override public int getSizeInventory() { return inventory.length; }
    @Override public ItemStack getStackInSlot(int slot) { return inventory[slot]; }
    @Override public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = inventory[slot]; if (stack == null) return null;
        if (stack.stackSize <= amount) { inventory[slot] = null; saveMagazineSlot(slot); markDirty(); return stack; }
        ItemStack result = stack.splitStack(amount); if (stack.stackSize == 0) inventory[slot] = null; saveMagazineSlot(slot); markDirty(); return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) { ItemStack stack = inventory[slot]; inventory[slot] = null; saveMagazineSlot(slot); return stack; }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) { inventory[slot] = stack; if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit(); if(slot==0) loadMagazine(); else saveMagazineSlot(slot); markDirty(); }
    @Override public String getInventoryName() { return "tile.psi.cad_assembler.name"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) { return !player.isDead && player.getDistanceSq(xCoord + .5D, yCoord + .5D, zCoord + .5D) <= 64D; }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot==0 ? stack != null && stack.getItem() instanceof ItemCreativeCAD : stack != null && stack.getItem() instanceof ItemSpellBullet; }
    private void loadMagazine() { for(int i=1;i<=MAGAZINE_SLOTS;i++) inventory[i]=inventory[0] == null ? null : ItemCreativeCAD.getBullet(inventory[0],i-1); }
    public void clearMagazineView() { for(int i=1;i<=MAGAZINE_SLOTS;i++) inventory[i]=null; markDirty(); }
    private void saveMagazineSlot(int slot) { if(inventory[0]!=null && inventory[0].getItem() instanceof ItemCreativeCAD) ItemCreativeCAD.setBullet(inventory[0],slot-1,inventory[slot]); }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); NBTTagList list = new NBTTagList(); for (int i=0;i<inventory.length;i++) if (inventory[i]!=null) { NBTTagCompound item = new NBTTagCompound(); item.setByte("Slot", (byte)i); inventory[i].writeToNBT(item); list.appendTag(item); } tag.setTag("Items", list); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); NBTTagList list=tag.getTagList("Items",10); for(int i=0;i<list.tagCount();i++){ NBTTagCompound item=list.getCompoundTagAt(i); int slot=item.getByte("Slot") & 255; if(slot<inventory.length) inventory[slot]=ItemStack.loadItemStackFromNBT(item); } }
}
