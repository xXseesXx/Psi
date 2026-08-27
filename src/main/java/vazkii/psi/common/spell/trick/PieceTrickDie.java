/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/PieceTrickDie.java:1
 * GTNH adaptation: ParamNumber<Double> vs Number; ItemStack net.minecraft.world.item -> net.minecraft.item; ICAD via
 * CADData/PsiAPI.
 */
package vazkii.psi.common.spell.trick;

import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickDie extends PieceTrick {

    SpellParam<Double> target;

    public PieceTrickDie(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1));
        setStatLabel(EnumSpellStat.PROJECTION, null);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamNumber(SpellParam.GENERIC_NAME_TARGET, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double timeVal = this.getParamValue(context, target)
            .doubleValue();
        if (Math.abs(timeVal) < 1) {
            context.stopped = true;
        }

        return null;
    }

}
