/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyGlowing.java:1
 * GTNH: Entity types adapted to 1.7.10.
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;

public class PieceSelectorNearbyGlowing extends PieceSelectorNearby {

    public PieceSelectorNearbyGlowing(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e != null && (e.isBurning() || e.getBrightness(1.0F) > 0.9F);
    }
}
