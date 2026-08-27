/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/entity/PieceOperatorEntityRaycast.java:25
 * Modern: Level.getEntities(AABB, Predicate), Vec3, AABB, ProjectileUtil. GTNH: World.getEntitiesWithinAABB +
 * AxisAlignedBB + Vec3 xCoord/yCoord/zCoord + manual raytrace.
 */
package vazkii.psi.common.spell.operator.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorEntityRaycast extends PieceOperator {

    SpellParam<Vector3> origin;
    SpellParam<Vector3> ray;
    SpellParam<Double> max;

    public PieceOperatorEntityRaycast(Spell spell) {
        super(spell);
    }

    public static Entity rayTraceEntities(net.minecraft.world.World world, Vec3 positionVector, Vec3 lookVector,
        double maxDistance) {
        double distance = maxDistance;
        Entity entity = null;

        Vec3 reachVector = Vec3.createVectorHelper(
            positionVector.xCoord + lookVector.xCoord * maxDistance,
            positionVector.yCoord + lookVector.yCoord * maxDistance,
            positionVector.zCoord + lookVector.zCoord * maxDistance);
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            Math.min(positionVector.xCoord, reachVector.xCoord) - 1,
            Math.min(positionVector.yCoord, reachVector.yCoord) - 1,
            Math.min(positionVector.zCoord, reachVector.zCoord) - 1,
            Math.max(positionVector.xCoord, reachVector.xCoord) + 1,
            Math.max(positionVector.yCoord, reachVector.yCoord) + 1,
            Math.max(positionVector.zCoord, reachVector.zCoord) + 1);
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, aabb);
        for (Entity entity1 : list) {
            if (entity1 == null || entity1.isDead || !entity1.canBeCollidedWith()) continue;
            float collisionBorderSize = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.boundingBox
                .expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
            net.minecraft.util.MovingObjectPosition mop = axisalignedbb.calculateIntercept(positionVector, reachVector);
            Vec3 clip = mop != null ? mop.hitVec : null;
            if (axisalignedbb.isVecInside(positionVector)) {
                if (0.0D < distance || distance == 0.0D) {
                    entity = entity1;
                    distance = 0.0D;
                }
            } else if (clip != null) {
                double distanceTo = positionVector.distanceTo(clip);
                if (distanceTo < distance) {
                    entity = entity1;
                    distance = distanceTo;
                }
            }
        }
        return entity;
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

        Entity entity = rayTraceEntities(context.focalPoint.worldObj, originVal.toVec3(), rayVal.toVec3(), maxLen);
        if (entity == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        if (entity.isDead || ISpellImmune.isImmune(entity))
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);

        return entity;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Entity.class;
    }
}
