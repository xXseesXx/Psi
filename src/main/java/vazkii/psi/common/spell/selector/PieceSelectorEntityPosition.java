/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.entity.Entity;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * Selector that returns the position of an entity as a Vector3.
 */
public class PieceSelectorEntityPosition extends PieceSelector {

    public SpellParam<Entity> target;

    public PieceSelectorEntityPosition(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetEntity = this.getParamValue(context, target);

        if (targetEntity == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        return new Vector3(targetEntity.posX, targetEntity.posY, targetEntity.posZ);
    }
}
