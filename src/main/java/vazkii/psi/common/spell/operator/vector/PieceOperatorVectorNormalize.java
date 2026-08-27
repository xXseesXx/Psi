/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/vector/PieceOperatorVectorNormalize.java:1
 * GTNH adaptation: ParamNumber<Double> (1.7.10) vs Number (1.21.1).
 */
package vazkii.psi.common.spell.operator.vector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorVectorNormalize extends PieceOperator {

    SpellParam<Vector3> vec1;

    public PieceOperatorVectorNormalize(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(vec1 = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 v1 = this.getParamValue(context, vec1);

        return v1.copy()
            .normalize();
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

}
