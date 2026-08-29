/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorNearbySmeltables.java:1
 */
package vazkii.psi.common.spell.selector.entity;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

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
