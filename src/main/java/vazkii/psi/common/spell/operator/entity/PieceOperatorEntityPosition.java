package vazkii.psi.common.spell.operator.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceOperator;

/** Returns an entity's position, using eye position for players like modern Psi. */
public class PieceOperatorEntityPosition extends PieceOperator {
    private SpellParam<Entity> target;

    public PieceOperatorEntityPosition(Spell spell) {
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
        Vector3 position = Vector3.fromEntity(entity);
        if (entity instanceof EntityPlayer) {
            position.add(0, entity.getEyeHeight(), 0);
        }
        return position;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
