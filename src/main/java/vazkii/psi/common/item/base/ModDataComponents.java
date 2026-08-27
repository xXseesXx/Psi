/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/item/base/ModDataComponents.java:22
 * Modern uses DeferredRegister<DataComponents> + DataComponentType<T> with Codec/StreamCodec.
 * GTNH 1.7.10 adaptation: No DataComponent registry exists — all components are stored as
 * NBTTagCompound keys on ItemStack.stackTagCompound. This class centralizes the key names
 * so call sites remain 1:1 with modern code (replace `stack.get(ModDataComponents.SPELL)`
 * with `ModDataComponents.getSpell(stack)` helper or direct NBT).
 * Mapping:
 * Modern: stack.set(ModDataComponents.SPELL, spell) -> 1.7.10: ItemSpellBullet.setSpell(stack, spell) / NBT "spell"
 * Modern: stack.get(ModDataComponents.CAD_DATA) -> 1.7.10: CADData NBT "PsiCADData"
 * Modern: stack.get(ModDataComponents.SELECTED_SLOT) -> 1.7.10: CADData NBT "SelectedSlot"
 * See docs/GTNH_MAPPING.md for full table.
 */
package vazkii.psi.common.item.base;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.core.handler.capability.CADData;
import vazkii.psi.common.item.ItemSpellBullet;

public final class ModDataComponents {

    // NBT key names — mirror modern component IDs for readability
    public static final String DST_POS = "dst_x";
    public static final String SRC_POS = "src_z";
    public static final String SPELL = "spell";
    public static final String SENSOR = "sensor";
    public static final String TIMES_CAST = "times_cast";
    public static final String SELECTED_CONTROL_SLOT = "selected_control_slot";
    public static final String CONTRIBUTOR = "psi_contributor_name";
    public static final String CAD_DATA = "PsiCADData";
    public static final String BULLETS = "bullets";
    public static final String SELECTED_SLOT = "SelectedSlot";
    public static final String REGEN_TIME = "regen_time";
    public static final String COMPONENTS = "components";

    // --- Helpers that mimic modern DataComponent accessors ---

    public static Spell getSpell(ItemStack stack) {
        return ItemSpellBullet.getSpell(stack);
    }

    public static void setSpell(ItemStack stack, Spell spell) {
        ItemSpellBullet.setSpell(stack, spell);
    }

    public static boolean hasSpell(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey(SPELL, 10);
    }

    public static int getSelectedSlot(ItemStack cad) {
        if (cad == null || !cad.hasTagCompound()) return 0;
        NBTTagCompound data = cad.getTagCompound()
            .getCompoundTag(CAD_DATA);
        return data.getInteger(SELECTED_SLOT);
    }

    public static void setSelectedSlot(ItemStack cad, int slot) {
        new CADData(cad).setSelectedSlot(slot);
    }

    public static int getRegenTime(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return 0;
        return stack.getTagCompound()
            .getInteger(REGEN_TIME);
    }

    public static void setRegenTime(ItemStack stack, int time) {
        if (stack == null) return;
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound()
            .setInteger(REGEN_TIME, time);
    }

    private ModDataComponents() {}
}
