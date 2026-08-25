package vazkii.psi.common.spell.operator.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.projectile.EntityThrowable;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceOperator;

/** Returns an entity's view vector, or motion vector for projectile-like entities. */
public class PieceOperatorEntityLook extends PieceOperator {

    private SpellParam<Entity> target;

    public PieceOperatorEntityLook(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getParamValue(context, target);
        if (entity == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (entity instanceof IProjectile || entity instanceof EntityThrowable
            || entity instanceof EntityFallingBlock) {
            return new Vector3(entity.motionX, entity.motionY, entity.motionZ);
        }
        return new Vector3(entity.getLookVec());
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
