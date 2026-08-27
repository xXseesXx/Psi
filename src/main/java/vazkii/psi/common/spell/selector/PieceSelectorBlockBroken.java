/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorBlockBroken.java:34
 * Modern: Vector3.fromBlockPos(positionBroken.getBlockPos()). GTNH: positionBroken is BlockPosCompat directly.
 */
package vazkii.psi.common.spell.selector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorBlockBroken extends PieceSelector {

    public PieceSelectorBlockBroken(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Object execute(SpellContext context) {
        if (context.positionBroken == null) return Vector3.zero.copy();
        return Vector3.fromBlockPos(context.positionBroken);
    }
}
