package vazkii.psi.common.block.tile.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.component.ItemCADAssembly;
import vazkii.psi.common.item.component.ItemCADBattery;
import vazkii.psi.common.item.component.ItemCADCore;
import vazkii.psi.common.item.component.ItemCADSocket;

/** Container for CAD assembly and the existing creative-CAD spell-bullet magazine. */
public class ContainerCADAssembler extends Container {
    public final TileCADAssembler assembler;

    public ContainerCADAssembler(EntityPlayer player, TileCADAssembler assembler) {
        this.assembler = assembler;
        addSlotToContainer(new Slot(assembler, TileCADAssembler.SLOT_OUTPUT, 120, 35) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override public void onPickupFromSlot(EntityPlayer p, ItemStack stack) { super.onPickupFromSlot(p, stack); assembler.craftCAD(); }
        });
        addSlotToContainer(new Slot(assembler, TileCADAssembler.SLOT_CAD, 35, 21) {
            @Override public void onPickupFromSlot(EntityPlayer p, ItemStack stack) { super.onPickupFromSlot(p, stack); assembler.clearMagazineView(); }
        });
        addSlotToContainer(new Slot(assembler, TileCADAssembler.SLOT_ASSEMBLY, 120, 91));
        addSlotToContainer(new Slot(assembler, TileCADAssembler.SLOT_CORE, 100, 91));
        addSlotToContainer(new Slot(assembler, TileCADAssembler.SLOT_SOCKET, 140, 91));
        addSlotToContainer(new Slot(assembler, TileCADAssembler.SLOT_BATTERY, 110, 111));

        for (int row = 0; row < 4; row++) for (int col = 0; col < 3; col++) {
            final int index = TileCADAssembler.SLOT_BULLET_START + col + row * 3;
            addSlotToContainer(new Slot(assembler, index, 17 + col * 18, 57 + row * 18) {
                private boolean enabled() { return assembler.isBulletSlotEnabled(index - TileCADAssembler.SLOT_BULLET_START); }
                @Override public boolean isItemValid(ItemStack stack) { return enabled() && super.isItemValid(stack); }
                @Override public boolean canTakeStack(EntityPlayer p) { return enabled() && super.canTakeStack(p); }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 48 + col * 18, 143 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(player.inventory, col, 48 + col * 18, 201));
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return assembler.isUseableByPlayer(player); }

    /** Shift-clicks route each CAD part to its category-locked input, including the CAD magazine. */
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        if (slot < 0 || slot >= inventorySlots.size()) return null;
        Slot source = (Slot) inventorySlots.get(slot);
        if (source == null || !source.getHasStack()) return null;

        ItemStack sourceStack = source.getStack();
        ItemStack original = sourceStack.copy();
        boolean moved;
        if (slot >= 18) {
            if (sourceStack.getItem() instanceof ItemCADAssembly) moved = mergeItemStack(sourceStack, 2, 3, false);
            else if (sourceStack.getItem() instanceof ItemCADCore) moved = mergeItemStack(sourceStack, 3, 4, false);
            else if (sourceStack.getItem() instanceof ItemCADSocket) moved = mergeItemStack(sourceStack, 4, 5, false);
            else if (sourceStack.getItem() instanceof ItemCADBattery) moved = mergeItemStack(sourceStack, 5, 6, false);
            else if (sourceStack.getItem() instanceof ItemCAD || sourceStack.getItem() instanceof ItemCreativeCAD) moved = mergeItemStack(sourceStack, 1, 2, false);
            else if (sourceStack.getItem() instanceof ItemSpellBullet) moved = mergeItemStack(sourceStack, 6, 18, false);
            else return null;
        } else {
            moved = mergeItemStack(sourceStack, 18, inventorySlots.size(), true);
        }

        if (!moved) return null;
        if (sourceStack.stackSize == 0) source.putStack(null);
        else source.onSlotChanged();
        source.onPickupFromSlot(player, sourceStack);
        return original;
    }
}
