/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorItemCount.java:27
 * Modern: getInventory().items Stream + getCount(). GTNH: getSizeInventory/getStackInSlot/stackSize + targetSlot
 * helper.
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorItemCount extends PieceSelector {

    public PieceSelectorItemCount(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int targetSlot;
        try {
            targetSlot = context.getTargetSlot();
        } catch (SpellRuntimeException e) {
            targetSlot = 0;
        }
        ItemStack toCount = context.caster.inventory.getStackInSlot(targetSlot);
        if (toCount == null) return 0.0;
        int total = 0;
        for (int i = 0; i < context.caster.inventory.getSizeInventory(); i++) {
            ItemStack stack = context.caster.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == toCount.getItem()
                && stack.getItemDamage() == toCount.getItemDamage()) {
                total += stack.stackSize;
            }
        }
        return (double) total;
    }
}
