/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/vector/PieceOperatorVectorRaycastAxis.java:22
 * Modern uses Level.clip(ClipContext), BlockHitResult, Direction.
 * GTNH adaptation: Entity.worldObj.rayTraceBlocks(Vec3, Vec3) + ForgeDirection via SpellHelpers/rangeLimitParam.
 * Mirrors 1.7.10 PieceOperatorVectorRaycast.java:32 logic.
 */
package vazkii.psi.common.spell.operator.vector;

import net.minecraft.util.MovingObjectPosition;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorVectorRaycastAxis extends PieceOperator {

    SpellParam<Vector3> origin;
    SpellParam<Vector3> ray;
    SpellParam<Double> max;

    public PieceOperatorVectorRaycastAxis(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(origin = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(ray = new ParamVector(SpellParam.GENERIC_NAME_RAY, SpellParam.GREEN, false, false));
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 originVal = this.getParamValue(context, origin);
        Vector3 rayVal = this.getParamValue(context, ray);

        if (originVal == null || rayVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        double maxLen = SpellHelpers.rangeLimitParam(this, context, max, SpellContext.MAX_DISTANCE);

        Vector3 end = originVal.copy()
            .add(
                rayVal.copy()
                    .normalize()
                    .multiply(maxLen));

        MovingObjectPosition pos = context.caster.worldObj.rayTraceBlocks(originVal.toVec3(), end.toVec3(), false);
        if (pos == null || pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        // Return facing as axial vector (mirrors Direction.getStepX/Y/Z)
        switch (pos.sideHit) {
            case 0:
                return new Vector3(0, -1, 0); // DOWN
            case 1:
                return new Vector3(0, 1, 0); // UP
            case 2:
                return new Vector3(0, 0, -1); // NORTH
            case 3:
                return new Vector3(0, 0, 1); // SOUTH
            case 4:
                return new Vector3(-1, 0, 0); // WEST
            case 5:
                return new Vector3(1, 0, 0); // EAST
            default:
                return new Vector3(0, 0, 0);
        }
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
