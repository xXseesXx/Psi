/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell.piece;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;

/**
 * Base class for all Trick pieces. Tricks are spell effects that interact with the world.
 * They don't return values (evaluate to Void).
 * 
 * 1.7.10 Backport: Simplified for barebones functionality.
 */
public abstract class PieceTrick extends SpellPiece {

    public PieceTrick(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1));
        setStatLabel(EnumSpellStat.PROJECTION, new StatLabel(1));
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.TRICK;
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException, ArithmeticException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
        meta.addStat(EnumSpellStat.PROJECTION, 1);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Void.class;
    }

    @Override
    public Object evaluate() {
        return null;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return null;
    }

    /**
     * Helper to multiply values safely without overflow.
     */
    public double multiplySafe(double v1, double... arr) throws SpellCompilationException {
        double a = v1;
        for (double b : arr) {
            a = a * b;
            if ((int) a < 0 || (int) a == Integer.MAX_VALUE) {
                throw new SpellCompilationException(SpellCompilationException.STAT_OVERFLOW);
            }
        }

        return a;
    }

}
