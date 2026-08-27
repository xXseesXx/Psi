/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/vector/PieceOperatorPlanarNormalVector.java:1
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

public class PieceOperatorPlanarNormalVector extends PieceOperator {

    SpellParam<Vector3> vector;

    public PieceOperatorPlanarNormalVector(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(vector = new ParamVector(SpellParam.GENERIC_NAME_VECTOR1, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 v = this.getParamValue(context, vector);
        return new Vector3(v.z, v.x, v.y);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
