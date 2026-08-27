/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/vector/PieceOperatorVectorAbsolute.java:1
 * GTNH adaptation: SpellHelpers now exists in 1.7.10 (api/spell/SpellHelpers.java:1) with
 * BlockPosCompat/ForgeDirection.
 */
package vazkii.psi.common.spell.operator.vector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorVectorAbsolute extends PieceOperator {

    SpellParam<Vector3> vec;

    public PieceOperatorVectorAbsolute(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(vec = new ParamVector(SpellParam.GENERIC_NAME_VECTOR, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 vector = SpellHelpers.getVector3(this, context, vec, false, false, false);
        return new Vector3(Math.abs(vector.x), Math.abs(vector.y), Math.abs(vector.z));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
