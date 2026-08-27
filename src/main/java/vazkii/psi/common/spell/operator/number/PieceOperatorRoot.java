/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/number/PieceOperatorRoot.java:1
 * GTNH adaptation: ParamNumber<Double> (1.7.10) vs Number (1.21.1); Level->World not needed here.
 */
package vazkii.psi.common.spell.operator.number;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorRoot extends PieceOperator {

    SpellParam<Double> num;
    SpellParam<Double> root;

    public PieceOperatorRoot(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(num = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.GREEN, false, false));
        addParam(root = new ParamNumber(SpellParam.GENERIC_NAME_ROOT, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double base = this.getParamValue(context, num)
            .doubleValue();
        double r = this.getParamValue(context, root)
            .doubleValue();
        if (base < 0 && r % 2 == 0) {
            throw new SpellRuntimeException(SpellRuntimeException.EVEN_ROOT_NEGATIVE_NUMBER);
        }
        return Math.pow(base, 1.0 / r);

    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
