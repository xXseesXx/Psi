/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/PieceTrickSmite.java:1
 * Modern: LightningBolt + BlockEvent. GTNH: EntityLightningBolt + World.addWeatherEffect.
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.entity.effect.EntityLightningBolt;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickSmite extends PieceTrick {

    SpellParam<Vector3> position;

    public PieceTrickSmite(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);
        if (positionVal == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!context.isInRadius(positionVal)) throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        EntityLightningBolt lightning = new EntityLightningBolt(
            context.focalPoint.worldObj,
            positionVal.x,
            positionVal.y,
            positionVal.z);
        context.focalPoint.worldObj.addWeatherEffect(lightning);
        return null;
    }
}
