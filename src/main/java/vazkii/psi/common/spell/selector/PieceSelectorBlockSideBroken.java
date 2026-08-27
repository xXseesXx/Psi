/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorBlockSideBroken.java:34
 * Modern: Vector3.fromDirection(positionBroken.getDirection()) where positionBroken is BlockHitResult.
 * GTNH: positionBroken is BlockPosCompat (no direction) — stub returns UP (0,1,0) for serviceable, TODO sideHit.
 */
package vazkii.psi.common.spell.selector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorBlockSideBroken extends PieceSelector {

    public PieceSelectorBlockSideBroken(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Object execute(SpellContext context) {
        // GTNH stub: no direction stored in BlockPosCompat — return UP vector
        // TODO: store sideHit in SpellContext.positionBroken as MovingObjectPosition
        return new Vector3(0, 1, 0);
    }
}
