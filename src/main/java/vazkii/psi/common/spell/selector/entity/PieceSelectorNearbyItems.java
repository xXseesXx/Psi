/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyItems.java:1
 * GTNH: Entity types Animal/WaterAnimal/Enemy -> EntityAnimal/EntityWaterMob/EntityMob; WorldHelper.
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;

public class PieceSelectorNearbyItems extends PieceSelectorNearby {

    public PieceSelectorNearbyItems(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e instanceof EntityItem;
    }
}
