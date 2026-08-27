/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorEidosChangelog.java:38
 * Modern: PlayerDataHandler.get(caster).eidosChangelog Vector3 list. GTNH: PlayerPsiHandler/eidos not yet ported — stub
 * returns caster position.
 * Serviceable stub, TODO eidos.
 */
package vazkii.psi.common.spell.selector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorEidosChangelog extends PieceSelector {

    public PieceSelectorEidosChangelog(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Object execute(SpellContext context) {
        // Stub: return caster position — modern returns last eidos changelog vector
        return Vector3.fromEntity(context.caster);
    }
}
