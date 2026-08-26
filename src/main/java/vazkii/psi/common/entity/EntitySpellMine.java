package vazkii.psi.common.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import vazkii.psi.api.spell.Spell;

/** A stationary grenade which fires after an entity enters and leaves its trigger radius. */
public class EntitySpellMine extends EntitySpellGrenade {

    private boolean triggered;
    private Entity target;

    public EntitySpellMine(World world) {
        super(world);
    }

    public EntitySpellMine(World world, EntityLivingBase caster, Spell spell) {
        super(world, caster, spell);
    }

    @Override
    public void onUpdate() {
        tickProjectile();
        if (worldObj.isRemote) return;
        motionX = motionY = motionZ = 0;
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> nearby = worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class,
            AxisAlignedBB.getBoundingBox(posX - 1, posY - 1, posZ - 1, posX + 1, posY + 1, posZ + 1));
        nearby.remove(getThrower());
        if (!triggered && !nearby.isEmpty()) {
            triggered = true;
            target = nearby.get(0);
        } else if (triggered && nearby.isEmpty()) {
            executeSpell(target);
            setDead();
        } else if (timeAlive >= 6000) setDead();
    }
}
