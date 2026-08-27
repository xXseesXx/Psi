/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorSavedVector.java:16
 * Modern: StatLabel(String,boolean), cad pattern variable, ItemStack. GTNH: StatLabel(String), ItemStack via
 * PsiAPI.getPlayerCAD, ICAD.
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorSavedVector extends PieceSelector {

    SpellParam<Double> number;

    public PieceSelectorSavedVector(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_NUMBER).mul(6));
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.BLUE, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double numberVal = this.<Double>getParamEvaluation(number);
        if (numberVal == null || numberVal <= 0 || numberVal != numberVal.intValue()) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }

        meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 6);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Integer numberVal = this.getParamValue(context, number)
            .intValue();

        int n = numberVal - 1;
        if (context.customData.containsKey("psi:SlotLocked" + n)) {
            throw new SpellRuntimeException(SpellRuntimeException.LOCKED_MEMORY);
        }

        ItemStack cadStack = PsiAPI.getPlayerCAD(context.caster);
        if (cadStack == null || !(cadStack.getItem() instanceof ICAD)) {
            throw new SpellRuntimeException(SpellRuntimeException.NO_CAD);
        }
        ICAD cad = (ICAD) cadStack.getItem();
        return cad.getStoredVector(cadStack, n);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
