/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorIsElytraFlying.java:10
 * GTNH 1.7.10 has no elytra - stub returns 0 (not flying).
 */
package vazkii.psi.common.spell.selector.entity;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorIsElytraFlying extends PieceSelector {

    public PieceSelectorIsElytraFlying(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) {
        return 0.0;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
