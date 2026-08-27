/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyFallingBlocks.java:1
 * GTNH: Entity types adapted to 1.7.10 net.minecraft.entity.*
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;

public class PieceSelectorNearbyFallingBlocks extends PieceSelectorNearby {

    public PieceSelectorNearbyFallingBlocks(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e instanceof EntityFallingBlock;
    }
}
