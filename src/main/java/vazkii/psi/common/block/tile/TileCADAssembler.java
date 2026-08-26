package vazkii.psi.common.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.component.ItemCADAssembly;
import vazkii.psi.common.item.component.ItemCADBattery;
import vazkii.psi.common.item.component.ItemCADCore;
import vazkii.psi.common.item.component.ItemCADSocket;

/** CAD assembler inventory: CAD magazine, four component inputs, twelve bullets, and a result. */
public class TileCADAssembler extends TileEntity implements IInventory {

    public static final int MAGAZINE_SLOTS = 12;
    public static final int SLOT_CAD = 0;
    public static final int SLOT_ASSEMBLY = 1;
    public static final int SLOT_CORE = 2;
    public static final int SLOT_SOCKET = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_BULLET_START = 5;
    public static final int SLOT_OUTPUT = SLOT_BULLET_START + MAGAZINE_SLOTS;
    private final ItemStack[] inventory = new ItemStack[SLOT_OUTPUT + 1];

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = inventory[slot];
        if (stack == null) return null;
        if (stack.stackSize <= amount) {
            inventory[slot] = null;
            changed(slot);
            return stack;
        }
        ItemStack result = stack.splitStack(amount);
        if (stack.stackSize == 0) inventory[slot] = null;
        changed(slot);
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        changed(slot);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        // This also protects automation and other callers that bypass Container/Slot validation.
        if (stack != null && !isItemValidForSlot(slot, stack)) return;
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        changed(slot);
    }

    @Override
    public String getInventoryName() {
        return "tile.psi.cad_assembler.name";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return !player.isDead && player.getDistanceSq(xCoord + .5D, yCoord + .5D, zCoord + .5D) <= 64D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null || slot == SLOT_OUTPUT) return false;
        if (slot == SLOT_CAD) return stack.getItem() instanceof ItemCAD || stack.getItem() instanceof ItemCreativeCAD;
        if (slot == SLOT_ASSEMBLY) return stack.getItem() instanceof ItemCADAssembly;
        if (slot == SLOT_CORE) return stack.getItem() instanceof ItemCADCore;
        if (slot == SLOT_SOCKET) return stack.getItem() instanceof ItemCADSocket;
        if (slot == SLOT_BATTERY) return stack.getItem() instanceof ItemCADBattery;
        return slot >= SLOT_BULLET_START && slot < SLOT_OUTPUT
            && stack.getItem() instanceof ItemSpellBullet
            && isBulletSlotEnabled(slot - SLOT_BULLET_START);
    }

    private void changed(int slot) {
        if (slot == SLOT_CAD) loadMagazine();
        else if (slot >= SLOT_BULLET_START && slot < SLOT_OUTPUT) saveMagazineSlot(slot);
        if (slot >= SLOT_ASSEMBLY && slot <= SLOT_BATTERY) {
            inventory[SLOT_OUTPUT] = null;
            updateCraftResult();
        }
        markDirty();
    }

    private void updateCraftResult() {
        if (inventory[SLOT_OUTPUT] != null) return;
        ItemStack assembly = inventory[SLOT_ASSEMBLY];
        if (assembly != null) inventory[SLOT_OUTPUT] = ItemCAD
            .createCAD(assembly, inventory[SLOT_CORE], inventory[SLOT_SOCKET], inventory[SLOT_BATTERY]);
    }

    /** Called by the output slot after a player takes the assembled CAD. */
    public void craftCAD() {
        inventory[SLOT_ASSEMBLY] = null;
        inventory[SLOT_CORE] = null;
        inventory[SLOT_SOCKET] = null;
        inventory[SLOT_BATTERY] = null;
        inventory[SLOT_OUTPUT] = null;
        markDirty();
    }

    public boolean isBulletSlotEnabled(int slot) {
        if (slot < 0 || slot >= MAGAZINE_SLOTS || inventory[SLOT_CAD] == null) return false;
        ItemStack cad = inventory[SLOT_CAD];
        return cad.getItem() instanceof ItemCreativeCAD
            || cad.getItem() instanceof ItemCAD && slot < ItemCAD.getMagazineSize(cad);
    }

    private void loadMagazine() {
        for (int i = 0; i < MAGAZINE_SLOTS; i++) {
            ItemStack cad = inventory[SLOT_CAD];
            inventory[SLOT_BULLET_START + i] = cad != null && isBulletSlotEnabled(i)
                ? (cad.getItem() instanceof ItemCreativeCAD ? ItemCreativeCAD.getBullet(cad, i)
                    : ItemCAD.getBullet(cad, i))
                : null;
        }
    }

    public void clearMagazineView() {
        for (int i = 0; i < MAGAZINE_SLOTS; i++) inventory[SLOT_BULLET_START + i] = null;
        markDirty();
    }

    private void saveMagazineSlot(int slot) {
        if (inventory[SLOT_CAD] != null) {
            int bulletSlot = slot - SLOT_BULLET_START;
            if (inventory[SLOT_CAD].getItem() instanceof ItemCreativeCAD)
                ItemCreativeCAD.setBullet(inventory[SLOT_CAD], bulletSlot, inventory[slot]);
            else if (inventory[SLOT_CAD].getItem() instanceof ItemCAD)
                ItemCAD.setBullet(inventory[SLOT_CAD], bulletSlot, inventory[slot]);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) if (inventory[i] != null) {
            NBTTagCompound item = new NBTTagCompound();
            item.setByte("Slot", (byte) i);
            inventory[i].writeToNBT(item);
            list.appendTag(item);
        }
        tag.setTag("Items", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            int slot = item.getByte("Slot") & 255;
            if (slot < inventory.length) inventory[slot] = ItemStack.loadItemStackFromNBT(item);
        }
    }
}
