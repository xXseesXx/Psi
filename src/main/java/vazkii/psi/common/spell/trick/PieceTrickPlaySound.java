/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/PieceTrickPlaySound.java:1
 * Modern: Level.playSound with SoundEvent. GTNH: World.playSoundAtEntity / playSoundEffect.
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.entity.Entity;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickPlaySound extends PieceTrick {

    SpellParam<Vector3> position;
    SpellParam<Double> volume;
    SpellParam<Double> pitch;

    public PieceTrickPlaySound(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(volume = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.GREEN, true, false));
        addParam(pitch = new ParamNumber(SpellParam.GENERIC_NAME_PITCH, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 pos = this.getParamValue(context, position);
        Double vol = this.getParamValueOrDefault(context, volume, 1.0);
        Double pit = this.getParamValueOrDefault(context, pitch, 1.0);
        if (pos == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!context.isInRadius(pos)) throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        Entity focal = context.focalPoint;
        focal.worldObj.playSoundEffect(pos.x, pos.y, pos.z, "random.orb", vol.floatValue(), pit.floatValue());
        return null;
    }
}
