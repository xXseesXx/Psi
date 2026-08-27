/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Helper for entity queries that modern does via Level.getEntities(AABB, Predicate).
 * GTNH: World.getEntitiesWithinAABB(Class, AxisAlignedBB) + manual distance/filter.
 * Used by Nearby* selectors and ClosestTo* operators.
 */
package vazkii.psi.common.spell;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public final class WorldHelper {

    public static EntityListWrapper getEntitiesInRadius(World world, Vector3 center, double radius) {
        return getEntitiesInRadius(world, center, radius, Entity.class, null);
    }

    public static EntityListWrapper getEntitiesInRadius(World world, Vector3 center, double radius,
        Class<? extends Entity> clazz, java.util.function.Predicate<Entity> filter) {
        if (world == null || center == null) return EntityListWrapper.EMPTY;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            center.x - radius,
            center.y - radius,
            center.z - radius,
            center.x + radius,
            center.y + radius,
            center.z + radius);
        List<Entity> raw = (List<Entity>) world.getEntitiesWithinAABB(clazz, box);
        List<Entity> filtered = new ArrayList<>();
        for (Entity e : raw) {
            if (e == null) continue;
            double dx = e.posX - center.x, dy = e.posY - center.y, dz = e.posZ - center.z;
            if (dx * dx + dy * dy + dz * dz > radius * radius) continue;
            if (filter != null && !filter.test(e)) continue;
            filtered.add(e);
        }
        return EntityListWrapper.make(filtered);
    }

    public static EntityListWrapper getEntitiesInRadius(SpellContext context, double radius) {
        return getEntitiesInRadius(context.focalPoint.worldObj, Vector3.fromEntity(context.focalPoint), radius);
    }

    public static EntityListWrapper getEntitiesInRadius(SpellContext context, Vector3 center, double radius) {
        return getEntitiesInRadius(context.focalPoint.worldObj, center, radius);
    }

    private WorldHelper() {}
}
