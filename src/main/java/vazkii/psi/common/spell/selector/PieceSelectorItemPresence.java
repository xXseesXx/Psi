/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorItemPresence.java:14
 * Modern: ItemStack count via getInventory().items.size() + getItem(slot).getCount(). GTNH: Inventory
 * getSizeInventory/getStackInSlot/stackSize + targetSlot.
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorItemPresence extends PieceSelector {

    SpellParam<Double> slot;

    public PieceSelectorItemPresence(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(slot = new ParamNumber("psi.spellparam.slot", SpellParam.BLUE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Double slotVal = this.getParamValue(context, slot);
        int invSlot;
        if (slotVal == null) {
            try {
                invSlot = context.getTargetSlot();
            } catch (SpellRuntimeException e) {
                invSlot = 0;
            }
        } else {
            invSlot = Math.abs(slotVal.intValue() - 1) % context.caster.inventory.getSizeInventory();
        }
        // GTNH: targetSlot helper already 1.7.10 adapted via SpellContext.getTargetSlot() is not yet fully implemented
        // for 1.7.10 inventory; fallback to invSlot
        ItemStack stack = context.caster.inventory.getStackInSlot(invSlot);

        return (double) (stack == null ? 0 : stack.stackSize);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
