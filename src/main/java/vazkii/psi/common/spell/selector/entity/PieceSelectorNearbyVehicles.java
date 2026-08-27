/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyVehicles.java:1
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;

public class PieceSelectorNearbyVehicles extends PieceSelectorNearby {

    public PieceSelectorNearbyVehicles(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e instanceof EntityMinecart || e instanceof EntityBoat;
    }
}
