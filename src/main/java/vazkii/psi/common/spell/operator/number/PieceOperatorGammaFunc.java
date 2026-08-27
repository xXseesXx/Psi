/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/number/PieceOperatorGammaFunc.java:1
 * GTNH adaptation: ParamNumber<Double> (1.7.10) vs Number (1.21.1); Level->World not needed here.
 */
package vazkii.psi.common.spell.operator.number;

import vazkii.psi.api.internal.math.Gamma;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorGammaFunc extends PieceOperator {

    SpellParam<Double> num1;

    public PieceOperatorGammaFunc(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1));
    }

    @Override
    public void initParams() {
        addParam(num1 = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double d1 = this.getParamValue(context, num1)
            .doubleValue();
        if (d1 <= 0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return Gamma.gamma(d1);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
