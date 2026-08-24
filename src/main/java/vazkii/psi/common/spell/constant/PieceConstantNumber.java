/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.constant;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;

/**
 * A constant number value.
 * Stripped version without GUI/rendering for barebones backport.
 */
public class PieceConstantNumber extends SpellPiece {

    public double value;

    public PieceConstantNumber(Spell spell) {
        super(spell);
        this.value = 0.0;
    }

    public PieceConstantNumber(Spell spell, double value) {
        super(spell);
        this.value = value;
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.CONSTANT;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Object evaluate() {
        return value;
    }

    @Override
    public Object execute(SpellContext context) {
        return evaluate();
    }
}
