/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyEnemies.java:1
 * GTNH: Entity types Animal/WaterAnimal/Enemy -> EntityAnimal/EntityWaterMob/EntityMob; WorldHelper.
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;

import vazkii.psi.api.spell.Spell;

public class PieceSelectorNearbyEnemies extends PieceSelectorNearby {

    public PieceSelectorNearbyEnemies(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e instanceof EntityMob;
    }
}
