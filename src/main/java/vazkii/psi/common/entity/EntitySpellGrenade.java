package vazkii.psi.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import vazkii.psi.api.spell.Spell;

/** A ballistic spell projectile that executes after a three-second fuse. */
public class EntitySpellGrenade extends EntitySpellProjectile {

    protected boolean landed;

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
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                ForgeDirection side = ForgeDirection.getOrientation(hit.sideHit);
                double offset = side == ForgeDirection.UP ? 0D : 0.1D;
                setPosition(
                    hit.hitVec.xCoord + side.offsetX * offset,
                    hit.hitVec.yCoord + side.offsetY * offset,
                    hit.hitVec.zCoord + side.offsetZ * offset);
            }
            motionX = motionY = motionZ = 0;
            if (hit != null && hit.entityHit != null) executeSpell(hit.entityHit);
        }
    }

    @Override
    public void onUpdate() {
        tickGrenade();
        if (!worldObj.isRemote && timeAlive >= 60) {
            executeSpell(null);
            setDead();
        }
    }

    /** Applies flight physics until impact, then holds the projectile at its impact location. */
    protected void tickGrenade() {
        if (landed) tickStationaryProjectile();
        else tickProjectile();
    }
}
