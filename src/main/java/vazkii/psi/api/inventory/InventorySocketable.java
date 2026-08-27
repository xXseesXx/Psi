package vazkii.psi.api.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.cad.ISocketable;

/**
 * Twelve-slot inventory view backed by an ISocketable item in another inventory.
 */
public class InventorySocketable implements IInventory {

    private final IInventory parent;
    private final int parentSlot;

    public InventorySocketable(IInventory parent, int parentSlot) {
        this.parent = parent;
        this.parentSlot = parentSlot;
    }

    private ItemStack getSocketableStack() {
        return parent.getStackInSlot(parentSlot);
    }

    private ISocketable getSocketable() {
        ItemStack stack = getSocketableStack();
        return ISocketable.isSocketable(stack) ? ISocketable.socketable(stack) : null;
    }

    @Override
    public int getSizeInventory() {
        return ISocketable.MAX_ASSEMBLER_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= getSizeInventory()) return null;

        ISocketable socketable = getSocketable();
        return socketable == null ? null : socketable.getBulletInSocket(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack current = getStackInSlot(slot);
        if (current == null) return null;

        if (current.stackSize <= amount) {
            setInventorySlotContents(slot, null);
            return current;
        }

        ItemStack result = current.splitStack(amount);
        setInventorySlotContents(slot, current);
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack current = getStackInSlot(slot);
        if (current != null) setInventorySlotContents(slot, null);
        return current;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack bullet) {
        if (slot < 0 || slot >= getSizeInventory()) return;

        ISocketable socketable = getSocketable();
        if (socketable == null) return;

        socketable.setBulletInSocket(slot, bullet);
        parent.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.psi.socketable";
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
    public void markDirty() {
        parent.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return parent.isUseableByPlayer(player);
    }

    @Override
    public void openInventory() {
        parent.openInventory();
    }

    @Override
    public void closeInventory() {
        parent.closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack bullet) {
        ISocketable socketable = getSocketable();
        return socketable != null && socketable.isItemValid(slot, bullet);
    }
}
