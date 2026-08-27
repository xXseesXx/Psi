/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/potion/PieceTrickPotionBase.java:22
 * Modern: Holder<MobEffect> + MobEffectInstance + LivingEntity. GTNH: Potion int id + PotionEffect + EntityLivingBase.
 */
package vazkii.psi.common.spell.trick.potion;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public abstract class PieceTrickPotionBase extends PieceTrick {

    SpellParam<Entity> target;
    SpellParam<Double> power;
    SpellParam<Double> time;

    public PieceTrickPotionBase(Spell spell) {
        super(spell);
        if (hasPower()) {
            setStatLabel(
                EnumSpellStat.POTENCY,
                new StatLabel(SpellParam.GENERIC_NAME_TIME).mul(
                    new StatLabel(SpellParam.GENERIC_NAME_POWER).square()
                        .mul(5)
                        .add(20)));
            setStatLabel(
                EnumSpellStat.COST,
                new StatLabel(SpellParam.GENERIC_NAME_TIME).mul(
                    new StatLabel(SpellParam.GENERIC_NAME_POWER).square()
                        .mul(25)
                        .add(40)));
        } else {
            setStatLabel(
                EnumSpellStat.POTENCY,
                new StatLabel(SpellParam.GENERIC_NAME_TIME).mul(5)
                    .add(20));
            setStatLabel(
                EnumSpellStat.COST,
                new StatLabel(SpellParam.GENERIC_NAME_TIME).mul(25)
                    .add(40));
        }
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
        if (hasPower()) {
            addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        }
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.BLUE, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerVal = 1D;
        if (hasPower()) {
            powerVal = this.<Double>getParamEvaluation(power);
        }
        Double timeVal = this.<Double>getParamEvaluation(time);

        if (powerVal == null || timeVal == null
            || powerVal <= 0
            || powerVal != powerVal.intValue()
            || timeVal <= 0
            || timeVal != timeVal.intValue()) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }

        meta.addStat(EnumSpellStat.POTENCY, 20 + getPotency(powerVal.intValue(), timeVal.intValue()));
        meta.addStat(EnumSpellStat.COST, 40 + getCost(powerVal.intValue(), timeVal.intValue()));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = this.getParamValue(context, target);

        context.verifyEntity(targetVal);
        if (!(targetVal instanceof EntityLivingBase)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        double powerVal = 1.0;
        if (hasPower()) {
            powerVal = this.getParamValue(context, power);
        }
        double timeVal = this.getParamValue(context, time);

        ((EntityLivingBase) targetVal).addPotionEffect(
            new PotionEffect(
                getPotion().id,
                Math.max(1, (int) timeVal) * 20,
                hasPower() ? Math.max(0, (int) powerVal - 1) : 0));

        return null;
    }

    public abstract Potion getPotion();

    public int getCost(int power, int time) throws SpellCompilationException {
        return (int) multiplySafe(getPotency(power, time) * 5);
    }

    public int getPotency(int power, int time) throws SpellCompilationException {
        return (int) multiplySafe(time, power, power, 5);
    }

    public boolean hasPower() {
        return true;
    }
}
