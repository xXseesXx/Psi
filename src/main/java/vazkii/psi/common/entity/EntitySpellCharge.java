package vazkii.psi.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import vazkii.psi.api.spell.Spell;

/** A persistent charge detonated by the Psi detonator item. */
public class EntitySpellCharge extends EntitySpellGrenade {

    public EntitySpellCharge(World world) {
        super(world);
    }

    public EntitySpellCharge(World world, EntityLivingBase caster, Spell spell) {
        super(world, caster, spell);
    }

    @Override
    public int getLiveTime() {
        return 6000;
    }

    @Override
    public int getParticleCount() {
        return 2;
    }

    @Override
    public void onUpdate() {
        tickGrenade();
        if (!worldObj.isRemote && timeAlive >= 6000) setDead();
    }

    public void detonate() {
        if (!worldObj.isRemote && !isDead) {
            executeSpell(null);
            setDead();
        }
    }
}
