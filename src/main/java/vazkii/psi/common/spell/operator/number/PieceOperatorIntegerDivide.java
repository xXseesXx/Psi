/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/number/PieceOperatorIntegerDivide.java:1
 * GTNH adaptation: ParamNumber<Double> (1.7.10) vs Number (1.21.1); Level->World not needed here.
 */
package vazkii.psi.common.spell.operator.number;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorIntegerDivide extends PieceOperator {

    SpellParam<Double> num1;
    SpellParam<Double> num2;
    SpellParam<Double> num3;

    public PieceOperatorIntegerDivide(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(num1 = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.RED, false, false));
        addParam(num2 = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER2, SpellParam.GREEN, false, false));
        addParam(num3 = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER3, SpellParam.YELLOW, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double d1 = this.getParamValue(context, num1)
            .doubleValue();
        Double d2 = this.getParamValue(context, num2)
            .doubleValue();
        Double d3 = this.getParamValue(context, num3);

        if ((d2.doubleValue() == 0 || d2.intValue() == 0)
            || (d3 != null && (d3.doubleValue() == 0 || d3.intValue() == 0))) {
            throw new SpellRuntimeException(SpellRuntimeException.DIVIDE_BY_ZERO);
        }
        double d4 = d3 != null ? (d1 / (d2.doubleValue() * d3.doubleValue())) : (d1 / d2.doubleValue());
        if (d4 < 0) {
            return Math.ceil(d4);
        }
        return Math.floor(d4);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

}
