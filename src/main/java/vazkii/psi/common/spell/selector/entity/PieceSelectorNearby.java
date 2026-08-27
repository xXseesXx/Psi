/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearby.java:1
 * Modern: AABB, Level.getEntitiesOfClass. GTNH: AxisAlignedBB, WorldHelper.getEntitiesInRadius.
 * GTNH adaptation: Vector3.fromVec3d(position()) -> Vector3.fromEntity, AABB -> AxisAlignedBB, Level.getEntities ->
 * WorldHelper.
 */
package vazkii.psi.common.spell.selector.entity;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceSelector;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;
import vazkii.psi.common.spell.WorldHelper;

public abstract class PieceSelectorNearby extends PieceSelector {

    SpellParam<Vector3> position;
    SpellParam<Double> radius;

    public PieceSelectorNearby(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, true, false));
        addParam(radius = new ParamNumber(SpellParam.GENERIC_NAME_RADIUS, SpellParam.GREEN, true, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double radiusVal = this.getParamEvaluationeOrDefault(radius, 2 * SpellContext.MAX_DISTANCE);
        if (radiusVal == null || radiusVal <= 0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValueOrDefault(context, position, Vector3.fromEntity(context.focalPoint));
        Double radiusVal = this.getParamValueOrDefault(context, radius, 2 * SpellContext.MAX_DISTANCE);

        if (positionVal == null) positionVal = Vector3.fromEntity(context.focalPoint);
        if (radiusVal == null) radiusVal = 2 * SpellContext.MAX_DISTANCE;

        Vector3 positionCenter = Vector3.fromEntity(context.focalPoint);

        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        double radiusClamped = Math.min(radiusVal, SpellContext.MAX_DISTANCE * 2);
        Predicate<Entity> pred = getTargetPredicate(context);

        // GTNH: intersect argument not needed — WorldHelper does radius + isInRadius check
        EntityListWrapper result = WorldHelper.getEntitiesInRadius(
            context.focalPoint.worldObj,
            positionVal,
            radiusClamped,
            Entity.class,
            e -> e != null && pred.test(e) && e != context.caster && e != context.focalPoint && context.isInRadius(e));
        return result;
    }

    public abstract Predicate<Entity> getTargetPredicate(SpellContext context);

    @Override
    public Class<?> getEvaluationType() {
        return EntityListWrapper.class;
    }
}
