package vazkii.psi.common.block.tile.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.api.cad.ICADComponent;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;

/**
 * Container for CAD assembly and its socketed Spell Bullet magazine.
 *
 * This 1.7.10 backport uses the TileCADAssembler's real inventory slots
 * directly. The 1.21.1 implementation uses InventorySocketable and an
 * item-handler based inventory, so that implementation should not be
 * copied directly into this branch.
 */
public class ContainerCADAssembler extends Container {

    private static final int OUTPUT_SLOT = TileCADAssembler.SLOT_OUTPUT;
    private static final int CAD_SLOT = TileCADAssembler.SLOT_CAD;
    private static final int ASSEMBLY_SLOT = TileCADAssembler.SLOT_ASSEMBLY;
    private static final int CORE_SLOT = TileCADAssembler.SLOT_CORE;
    private static final int SOCKET_SLOT = TileCADAssembler.SLOT_SOCKET;
    private static final int BATTERY_SLOT = TileCADAssembler.SLOT_BATTERY;
    private static final int DYE_SLOT = TileCADAssembler.SLOT_DYE;

    private static final int COMPONENT_START = ASSEMBLY_SLOT;

    private static final int BULLET_START = TileCADAssembler.SLOT_BULLET_START;
    private static final int BULLET_COUNT = TileCADAssembler.MAGAZINE_SLOTS;
    private static final int BULLET_END = BULLET_START + BULLET_COUNT;

    private static final int PLAYER_START = BULLET_END;
    private static final int HOTBAR_END = PLAYER_START + 36;

    public final TileCADAssembler assembler;

    public ContainerCADAssembler(EntityPlayer player, TileCADAssembler assembler) {
        this.assembler = assembler;

        /*
         * Output slot.
         */
        addSlotToContainer(new Slot(
            assembler,
            OUTPUT_SLOT,
            120,
            35) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
                super.onPickupFromSlot(player, stack);
                assembler.craftCAD();
            }
        });

        /*
         * CAD slot.
         */
        addSlotToContainer(new Slot(
            assembler,
            CAD_SLOT,
            35,
            21) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return assembler.isItemValidForSlot(CAD_SLOT, stack);
            }

            @Override
            public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
                super.onPickupFromSlot(player, stack);
                assembler.clearMagazineView();
            }
        });

        /*
         * CAD component inputs.
         */
        addInputSlot(ASSEMBLY_SLOT, 120, 91);
        addInputSlot(CORE_SLOT, 100, 91);
        addInputSlot(SOCKET_SLOT, 140, 91);
        addInputSlot(BATTERY_SLOT, 110, 111);
        addInputSlot(DYE_SLOT, 130, 111);

        /*
         * CAD magazine.
         *
         * These are real TileCADAssembler inventory slots in the 1.7.10
         * backport. Each magazine slot only accepts a single spell bullet.
         */
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                final int slot = BULLET_START + col + row * 3;

                addSlotToContainer(new Slot(
                    assembler,
                    slot,
                    17 + col * 18,
                    57 + row * 18) {

                    private boolean enabled() {
                        return assembler.isBulletSlotEnabled(
                            slot - BULLET_START);
                    }

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return enabled()
                            && assembler.isItemValidForSlot(slot, stack);
                    }

                    @Override
                    public boolean canTakeStack(EntityPlayer player) {
                        return enabled() && super.canTakeStack(player);
                    }

                    @Override
                    public int getSlotStackLimit() {
                        return 1;
                    }
                });
            }
        }

        /*
         * Player inventory.
         */
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                    player.inventory,
                    col + row * 9 + 9,
                    48 + col * 18,
                    143 + row * 18));
            }
        }

        /*
         * Player hotbar.
         */
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(
                player.inventory,
                col,
                48 + col * 18,
                201));
        }
    }

    private void addInputSlot(final int slot, int x, int y) {
        addSlotToContainer(new Slot(
            assembler,
            slot,
            x,
            y) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return assembler.isItemValidForSlot(slot, stack);
            }
        });
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return assembler.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= inventorySlots.size()) {
            return null;
        }

        Slot source = (Slot) inventorySlots.get(slotIndex);

        if (source == null || !source.getHasStack()) {
            return null;
        }

        ItemStack sourceStack = source.getStack();
        ItemStack original = sourceStack.copy();

        boolean moved;

        /*
         * Player inventory -> assembler.
         */
        if (slotIndex >= PLAYER_START) {
            moved = moveFromPlayer(sourceStack);
        }

        /*
         * Assembler -> player inventory.
         */
        else {
            moved = mergeItemStack(
                sourceStack,
                PLAYER_START,
                HOTBAR_END,
                true);
        }

        if (!moved) {
            return null;
        }

        if (sourceStack.stackSize == 0) {
            source.putStack(null);
        } else {
            source.onSlotChanged();
        }

        source.onPickupFromSlot(player, sourceStack);

        return original;
    }

    private boolean moveFromPlayer(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }

        Object item = stack.getItem();

        /*
         * Colorizers have their own CAD interface and must be handled before
         * the generic ICADComponent path.
         */
        if (item instanceof ICADColorizer) {
            return mergeItemStack(
                stack,
                DYE_SLOT,
                DYE_SLOT + 1,
                false);
        }

        /*
         * Normal CAD components.
         */
        if (item instanceof ICADComponent) {
            EnumCADComponent type =
                ((ICADComponent) item).getComponentType(stack);

            if (type == null) {
                return false;
            }

            int target = COMPONENT_START + type.ordinal();

            return mergeItemStack(
                stack,
                target,
                target + 1,
                false);
        }

        /*
         * Normal and creative CADs.
         */
        if (item instanceof ItemCAD
            || item instanceof ItemCreativeCAD) {

            return mergeItemStack(
                stack,
                CAD_SLOT,
                CAD_SLOT + 1,
                false);
        }

        /*
         * Spell containers, including ItemSpellBullet.
         *
         * Do this through the ISpellAcceptor API rather than checking
         * ItemSpellBullet directly. The 1.7.10 adapter can therefore expose
         * bullets exactly like the modern capability implementation does.
         */
        if (ISpellAcceptor.isContainer(stack)) {
            return moveBulletToMagazine(stack);
        }

        /*
         * Other socketable items belong in the CAD slot.
         */
        if (ISocketable.isSocketable(stack)) {
            return mergeItemStack(
                stack,
                CAD_SLOT,
                CAD_SLOT + 1,
                false);
        }

        /*
         * Nothing recognized by the assembler.
         */
        return false;
    }

    /**
     * Moves spell bullets into the physical magazine slots.
     *
     * A CAD magazine stores exactly one bullet in each slot, so this must
     * not use mergeItemStack(). Doing so can merge a whole player stack into
     * one slot before the slot-size limit is applied.
     */
    private boolean moveBulletToMagazine(ItemStack sourceStack) {
        boolean moved = false;

        for (int slot = BULLET_START;
             slot < BULLET_END && sourceStack.stackSize > 0;
             slot++) {

            Slot target = (Slot) inventorySlots.get(slot);

            if (target.getHasStack()) {
                continue;
            }

            if (!target.isItemValid(sourceStack)) {
                continue;
            }

            ItemStack bullet = sourceStack.copy();
            bullet.stackSize = 1;

            target.putStack(bullet);

            sourceStack.stackSize--;
            moved = true;
        }

        return moved;
    }
}
