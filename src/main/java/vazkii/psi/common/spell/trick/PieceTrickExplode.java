/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Trick that creates an explosion at a specified position.
 * Simplified version for 1.7.10 - no liquid detection, always uses fire.
 */
public class PieceTrickExplode extends PieceTrick {

    public SpellParam<Vector3> position;
    public SpellParam<Double> power;

    private static final double MIN_POWER = 0.5;

    public PieceTrickExplode(Spell spell) {
        super(spell);
        String powerName = StatCollector.translateToLocal(SpellParam.GENERIC_NAME_POWER);
        setStatLabel(
            EnumSpellStat.POTENCY,
            new StatLabel(powerName).max(MIN_POWER)
                .mul(70)
                .floor());
        setStatLabel(
            EnumSpellStat.COST,
            new StatLabel(powerName).max(MIN_POWER)
                .mul(210)
                .floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = this.<Double>getParamEvaluation(power);
        if (powerVal == null || powerVal.doubleValue() <= 0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        powerVal = Math.max(MIN_POWER, powerVal.doubleValue());
        meta.addStat(EnumSpellStat.POTENCY, (int) (powerVal.doubleValue() * 70));
        meta.addStat(EnumSpellStat.COST, (int) (powerVal.doubleValue() * 210));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Double powerVal = this.getParamValue(context, power);

        World world = context.caster.worldObj;

        // 1.7.10 explosion API: createExplosion(entity, x, y, z, power, causeFire)
        // entity = caster for attribution
        // causeFire = true for visual effect
        world.createExplosion(context.caster, positionVal.x, positionVal.y, positionVal.z, powerVal.floatValue(), true // cause
                                                                                                                       // fire
        );

        return null;
    }
}
