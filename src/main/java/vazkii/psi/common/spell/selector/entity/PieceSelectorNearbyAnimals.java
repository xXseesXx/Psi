/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyAnimals.java:1
 * GTNH: Entity types Animal/WaterAnimal/Enemy -> EntityAnimal/EntityWaterMob/EntityMob; WorldHelper.
 */
package vazkii.psi.common.spell.selector.entity;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

public class PieceSelectorNearbyAnimals extends PieceSelectorNearby {

    public PieceSelectorNearbyAnimals(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> (e instanceof EntityAnimal || e instanceof EntityWaterMob) && !(e instanceof EntityMob);
    }
}
