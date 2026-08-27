/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorRulerVector.java:1
 * Modern: ItemVectorRuler.getRulerVector(Player). GTNH: EntityPlayer version.
 */
package vazkii.psi.common.spell.selector;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;
import vazkii.psi.common.item.ItemVectorRuler;

public class PieceSelectorRulerVector extends PieceSelector {

    public PieceSelectorRulerVector(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Object execute(SpellContext context) {
        return ItemVectorRuler.getRulerVector(context.caster);
    }
}
