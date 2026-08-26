package vazkii.psi.common.core.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import vazkii.psi.common.entity.EntitySpellCharge;

public final class DetonationHandler {

    private DetonationHandler() {}

    @SuppressWarnings("unchecked")
    public static void detonate(World world, EntityPlayer player, double x, double y, double z, double range) {
        List<EntitySpellCharge> charges = new ArrayList<EntitySpellCharge>();
        for (Entity entity : (List<Entity>) world.loadedEntityList) {
            if (entity instanceof EntitySpellCharge && entity.getDistanceSq(x, y, z) <= range * range)
                charges.add((EntitySpellCharge) entity);
        }
        for (EntitySpellCharge charge : charges) charge.detonate();
    }
}
