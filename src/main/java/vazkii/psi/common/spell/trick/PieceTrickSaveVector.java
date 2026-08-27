/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/PieceTrickSaveVector.java:21
 * Modern: StatLabel(String,boolean), cad pattern variable. GTNH: StatLabel(String), ItemStack via PsiAPI.getPlayerCAD,
 * ICAD.setStoredVector.
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickSaveVector extends PieceTrick {

    public static final String KEY_SLOT_LOCKED = "psi:SlotLocked";

    SpellParam<Double> number;
    SpellParam<Vector3> target;

    public PieceTrickSaveVector(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(2));
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_NUMBER).mul(8));
        setStatLabel(EnumSpellStat.PROJECTION, null);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.BLUE, false, true));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.RED, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);

        Double numberVal = this.<Double>getParamEvaluation(number);
        if (numberVal == null || numberVal <= 0 || numberVal != numberVal.intValue()) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }

        meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 8);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Double numberVal = this.getParamValue(context, number);
        Vector3 targetVal = this.getParamValue(context, target);

        if (numberVal == null || targetVal == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        int n = numberVal.intValue() - 1;

        if (context.customData.containsKey(KEY_SLOT_LOCKED + n)) {
            return null;
        }

        ItemStack cadStack = PsiAPI.getPlayerCAD(context.caster);
        if (cadStack == null || !(cadStack.getItem() instanceof ICAD)) {
            throw new SpellRuntimeException(SpellRuntimeException.NO_CAD);
        }
        ICAD cad = (ICAD) cadStack.getItem();
        cad.setStoredVector(cadStack, n, targetVal);

        context.customData.put(KEY_SLOT_LOCKED + n, 0);

        return null;
    }
}
