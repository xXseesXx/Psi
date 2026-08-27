/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/number/PieceOperatorRandom.java:1
 * GTNH adaptation: ParamNumber<Double> (1.7.10) vs Number (1.21.1); Level->World not needed here.
 */
package vazkii.psi.common.spell.operator.number;

import java.util.concurrent.ThreadLocalRandom;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorRandom extends PieceOperator {

    SpellParam<Double> max;
    SpellParam<Double> min;

    public PieceOperatorRandom(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.BLUE, false, false));
        addParam(min = new ParamNumber(SpellParam.GENERIC_NAME_MIN, SpellParam.RED, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int maxVal = this.getParamValue(context, max)
            .intValue();
        int minVal = this.getParamValueOrDefault(context, min, 0D)
            .intValue();

        if (maxVal - minVal <= 0) {
            throw new SpellRuntimeException(SpellRuntimeException.NEGATIVE_NUMBER);
        }

        return (double) (ThreadLocalRandom.current()
            .nextInt(maxVal - minVal) + minVal);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

}
