/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/entity/PieceTrickBlink.java:22
 * Modern: ServerPlayer + MessageBlink + Vec3 look. GTNH: EntityPlayerMP + PacketBlink + Vec3 xCoord/yCoord/zCoord +
 * getLookVec.
 */
package vazkii.psi.common.spell.trick.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;

import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickBlink extends PieceTrick {

    SpellParam<Entity> target;
    SpellParam<Double> distance;

    public PieceTrickBlink(Spell spell) {
        super(spell);
        setStatLabel(
            EnumSpellStat.POTENCY,
            new StatLabel(SpellParam.GENERIC_NAME_DISTANCE).abs()
                .mul(30));
        setStatLabel(
            EnumSpellStat.COST,
            new StatLabel(SpellParam.GENERIC_NAME_DISTANCE).abs()
                .mul(40));
    }

    public static void blink(SpellContext context, Entity e, double dist) throws SpellRuntimeException {
        context.verifyEntity(e);
        if (!context.isInRadius(e)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Vec3 look = e.getLookVec();

        double offX = look.xCoord * dist;
        double offY = e.equals(context.caster) ? look.yCoord * dist : Math.max(0, look.yCoord * dist);
        double offZ = look.zCoord * dist;

        double nx = e.posX + offX;
        double ny = e.posY + offY;
        double nz = e.posZ + offZ;
        if (e instanceof EntityPlayerMP) {
            // 1.7.10: setPositionAndUpdate syncs to client; setPosition alone leaves client desynced (rubber-band,
            // “doesn’t work”)
            ((EntityPlayerMP) e).setPositionAndUpdate(nx, ny, nz);
        } else {
            e.setPosition(nx, ny, nz);
        }
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
        addParam(distance = new ParamNumber(SpellParam.GENERIC_NAME_DISTANCE, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double distanceVal = this.<Double>getParamEvaluation(distance);
        if (distanceVal == null) {
            distanceVal = 1D;
        }

        meta.addStat(EnumSpellStat.POTENCY, (int) (Math.abs(distanceVal) * 30));
        meta.addStat(EnumSpellStat.COST, (int) (Math.abs(distanceVal) * 40));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = this.getParamValue(context, target);
        Double distanceVal = this.getParamValue(context, distance);
        if (distanceVal == null) distanceVal = 1D;

        blink(context, targetVal, distanceVal);

        return null;
    }
}
