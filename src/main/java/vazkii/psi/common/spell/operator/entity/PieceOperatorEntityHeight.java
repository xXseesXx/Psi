/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/entity/PieceOperatorEntityHeight.java:20
 * Modern: e.getBbHeight() (1.21 AABB height). GTNH: e.height (1.7.10 Entity.height field).
 */
package vazkii.psi.common.spell.operator.entity;

import net.minecraft.entity.Entity;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorEntityHeight extends PieceOperator {

    SpellParam<Entity> target;

    public PieceOperatorEntityHeight(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity e = this.getParamValue(context, target);

        if (e == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        return (double) e.height;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
