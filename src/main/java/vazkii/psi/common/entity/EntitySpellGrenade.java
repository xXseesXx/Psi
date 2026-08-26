package vazkii.psi.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import vazkii.psi.api.spell.Spell;

/** A ballistic spell projectile that executes after a three-second fuse. */
public class EntitySpellGrenade extends EntitySpellProjectile {

    private boolean landed;

    public EntitySpellGrenade(World world) {
        super(world);
    }

    public EntitySpellGrenade(World world, EntityLivingBase caster, Spell spell) {
        super(world, caster, spell);
    }

    @Override
    protected float getGravityVelocity() {
        return .05F;
    }

    @Override
    protected void onImpact(MovingObjectPosition hit) {
        if (!landed) {
            landed = true;
            motionX = motionY = motionZ = 0;
            if (hit != null && hit.entityHit != null) executeSpell(hit.entityHit);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote && timeAlive >= 60) {
            executeSpell(null);
            setDead();
        }
    }
}
