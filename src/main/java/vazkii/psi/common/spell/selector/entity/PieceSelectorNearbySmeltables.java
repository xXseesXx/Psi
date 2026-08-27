/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbySmeltables.java:1
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.entity.Entity;

public class PieceSelectorNearbySmeltables extends PieceSelectorNearby {

    public PieceSelectorNearbySmeltables(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return e -> {
            if (e instanceof EntityItem ei) {
                ItemStack s = ei.getEntityItem();
                return FurnaceRecipes.smelting()
                    .getSmeltingResult(s) != null;
            }
            return false;
        };
    }
}
