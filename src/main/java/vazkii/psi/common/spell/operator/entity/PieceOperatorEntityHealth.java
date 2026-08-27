/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/entity/PieceOperatorEntityHealth.java:21
 * Modern: Entity LivingEntity getHealth/getMaxHealth. GTNH: EntityLivingBase 1.7.10 same methods.
 */
package vazkii.psi.common.spell.operator.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorEntityHealth extends PieceOperator {

    SpellParam<Entity> target;

    public PieceOperatorEntityHealth(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = this.getNotNullParamValue(context, target);
        if (!(entity instanceof EntityLivingBase)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        EntityLivingBase living = (EntityLivingBase) entity;
        return (double) living.getHealth() / (double) living.getMaxHealth();
    }
}
