/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/infusion/PieceTrickEbonyIvory.java:1
 */
package vazkii.psi.common.spell.trick.infusion;

import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.piece.PieceCraftingTrick;

public class PieceTrickEbonyIvory extends PieceTrickGreaterInfusion {

    public PieceTrickEbonyIvory(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(250));
        setStatLabel(EnumSpellStat.COST, new StatLabel(3000));
    }

    @Override
    protected void addPotencyAndCost(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.POTENCY, 250);
        meta.addStat(EnumSpellStat.COST, 3000);
    }

    @Override
    public boolean canCraft(PieceCraftingTrick trick) {
        return trick instanceof PieceTrickEbonyIvory;
    }
}
