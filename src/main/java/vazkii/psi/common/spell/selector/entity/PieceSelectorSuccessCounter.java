/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorSuccessCounter.java:10
 * GTNH: IPsiEventArmor + ModDataComponents.TIMES_CAST stub returns 0.
 */
package vazkii.psi.common.spell.selector.entity;

import vazkii.psi.api.spell.Spell;

public class PieceSelectorSuccessCounter extends PieceSelector {

    public PieceSelectorSuccessCounter(Spell spell) {
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
