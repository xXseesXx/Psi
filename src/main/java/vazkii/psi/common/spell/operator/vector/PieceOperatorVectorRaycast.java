package vazkii.psi.common.spell.operator.vector;

import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/** Raycasts from an arbitrary origin and direction, matching modern Psi's vector raycast. */
public class PieceOperatorVectorRaycast extends PieceOperator {

    private SpellParam<Vector3> origin;
    private SpellParam<Vector3> ray;
    private SpellParam<Double> max;

    public PieceOperatorVectorRaycast(Spell spell) {
        super(spell);
    }

    public static MovingObjectPosition raycast(Entity entity, double length) {
        Vector3 origin = Vector3.fromEntity(entity)
            .add(0, entity.getEyeHeight(), 0);
        return raycast(entity, origin, new Vector3(entity.getLookVec()), length);
    }

    public static MovingObjectPosition raycast(Entity entity, Vector3 origin, Vector3 ray, double length) {
        Vector3 end = origin.copy()
            .add(
                ray.copy()
                    .normalize()
                    .multiply(length));
        // stopOnLiquid=false matches ClipContext.Fluid.NONE in modern Psi.
        return entity.worldObj.rayTraceBlocks(origin.toVec3(), end.toVec3(), false);
    }

    @Override
    public void initParams() {
        addParam(origin = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(ray = new ParamVector(SpellParam.GENERIC_NAME_RAY, SpellParam.GREEN, false, false));
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 originValue = getParamValue(context, origin);
        Vector3 rayValue = getParamValue(context, ray);
        if (originValue == null || rayValue == null || rayValue.isZero()) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        Double maxValue = getParamValue(context, max);
        double length = maxValue == null ? SpellContext.MAX_DISTANCE : maxValue;
        if (Double.isNaN(length) || Double.isInfinite(length)) {
            throw new SpellRuntimeException(SpellRuntimeException.NAN);
        }
        length = Math.max(0, Math.min(SpellContext.MAX_DISTANCE, length));
        MovingObjectPosition hit = raycast(context.caster, originValue, rayValue, length);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        return new Vector3(hit.blockX, hit.blockY, hit.blockZ);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
