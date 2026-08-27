/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/constant/PieceConstantTau.java:1
 */
package vazkii.psi.common.spell.constant;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;

public class PieceConstantTau extends SpellPiece {

    public PieceConstantTau(Spell spell) {
        super(spell);
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
        return 2 * Math.PI;
    }

    @Override
    public Object execute(SpellContext context) {
        return evaluate();
    }

}
