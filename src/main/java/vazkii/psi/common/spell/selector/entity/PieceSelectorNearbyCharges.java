/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbyCharges.java:10
 * GTNH: EntitySpellCharge owner check stub — returns nearby EntitySpellCharge without owner filter, serviceable.
 */
package vazkii.psi.common.spell.selector.entity;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.entity.EntitySpellCharge;

public class PieceSelectorNearbyCharges extends PieceSelectorNearby {

    public PieceSelectorNearbyCharges(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> e instanceof EntitySpellCharge;
    }
}
