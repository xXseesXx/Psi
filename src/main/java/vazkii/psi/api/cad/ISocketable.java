package vazkii.psi.api.cad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.common.lib.LibResources;

/**
 * Defines an item that can contain Spell Bullets.
 *
 * This is the 1.7.10 form of the modern socketable API. Capability lookup is
 * replaced by a direct item-interface check.
 */
public interface ISocketable {

    List<ResourceLocation> signs = Arrays.asList(
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 0)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 1)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 2)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 3)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 4)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 5)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 6)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 7)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 8)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 9)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 10)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 11)),
        new ResourceLocation(String.format(LibResources.GUI_SIGN, 12)));

    int MAX_ASSEMBLER_SLOTS = 12;

    static String getSocketedItemName(ItemStack stack, String fallback) {
        if (stack == null || !isSocketable(stack)) return fallback;

        ItemStack bullet = socketable(stack).getSelectedBullet();
        return bullet == null ? fallback : bullet.getDisplayName();
    }

    static boolean isSocketable(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ISocketable;
    }

    static ISocketable socketable(ItemStack stack) {
        if (!isSocketable(stack)) throw new NullPointerException();
        return (ISocketable) stack.getItem();
    }

    boolean isSocketSlotAvailable(int slot);

    default List<Integer> getRadialMenuSlots() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < MAX_ASSEMBLER_SLOTS; i++) {
            if (isSocketSlotAvailable(i)) list.add(i);
        }
        return list;
    }

    default List<ResourceLocation> getRadialMenuIcons() {
        return signs;
    }

    ItemStack getBulletInSocket(int slot);

    void setBulletInSocket(int slot, ItemStack bullet);

    int getSelectedSlot();

    void setSelectedSlot(int slot);

    default int getLastSlot() {
        int slot = 0;
        while (slot + 1 < MAX_ASSEMBLER_SLOTS && isSocketSlotAvailable(slot + 1)) {
            slot++;
        }
        return slot;
    }

    default ItemStack getSelectedBullet() {
        return getBulletInSocket(getSelectedSlot());
    }

    default boolean isItemValid(int slot, ItemStack bullet) {
        if (!isSocketSlotAvailable(slot)) return false;
        return bullet != null && ISpellAcceptor.isContainer(bullet);
    }

    /**
     * CAD-specific loopcasting is supplied by ICADData once that API is ported.
     */
    default boolean canLoopcast() {
        return false;
    }
}
