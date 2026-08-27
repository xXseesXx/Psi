/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/constant/PieceConstantWrapper.java:1
 * GTNH adaptation: ParamNumber<Double> vs Number.
 */
package vazkii.psi.common.spell.constant;

import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;

public class PieceConstantWrapper extends SpellPiece {

    SpellParam<Double> target;
    SpellParam<Double> max;

    boolean evaluating = false;

    public PieceConstantWrapper(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamNumber(SpellParam.GENERIC_NAME_TARGET, SpellParam.RED, false, false));
        addParam(max = new ParamNumber("psi.spellparam.constant", SpellParam.GREEN, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Double targetVal = this.getParamValue(context, target);
        Double maxVal = this.getParamValue(context, max);
        if (targetVal == null) targetVal = 0D;
        if (maxVal == null) maxVal = 0D;

        if (maxVal > 0) {
            return Math.min(maxVal, Math.abs(targetVal));
        } else if (maxVal < 0) {
            return Math.max(maxVal, -Math.abs(targetVal));
        } else {
            return 0.0;
        }
    }

    @Override
    public Object evaluate() throws SpellCompilationException {
        if (evaluating) {
            return 0.0;
        }

        evaluating = true;
        Object ret = getParamEvaluation(max);
        evaluating = false;

        return ret;
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.CONSTANT;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
