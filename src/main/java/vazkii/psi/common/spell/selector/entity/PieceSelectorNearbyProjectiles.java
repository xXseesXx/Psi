/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyProjectiles.java:1
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;

public class PieceSelectorNearbyProjectiles extends PieceSelectorNearby {

    public PieceSelectorNearbyProjectiles(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e instanceof EntityArrow || e instanceof EntityThrowable;
    }
}
