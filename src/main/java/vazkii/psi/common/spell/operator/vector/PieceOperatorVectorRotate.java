/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/vector/PieceOperatorVectorRotate.java:1
 * GTNH adaptation: ParamNumber<Double> (1.7.10) vs Number (1.21.1); Vector3.rotate now via Quat.
 */
package vazkii.psi.common.spell.operator.vector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorVectorRotate extends PieceOperator {

    private SpellParam<Vector3> vector;
    private SpellParam<Vector3> axis;
    private SpellParam<Double> angle;

    public PieceOperatorVectorRotate(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(vector = new ParamVector(SpellParam.GENERIC_NAME_VECTOR, SpellParam.RED, false, false));
        addParam(axis = new ParamVector(SpellParam.GENERIC_NAME_AXIS, SpellParam.CYAN, false, false));
        addParam(angle = new ParamNumber(SpellParam.GENERIC_NAME_ANGLE, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 v = this.getParamValue(context, vector);
        Vector3 a = this.getParamValue(context, axis);
        Double anVal = this.getParamValue(context, angle);
        if (v == null || a == null || anVal == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        double an = anVal;
        return v.copy()
            .rotate(an, a.copy());
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
