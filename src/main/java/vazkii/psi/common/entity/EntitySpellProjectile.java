package vazkii.psi.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.Psi;

/**
 * A projectile entity that carries and executes a spell on impact.
 */
public class EntitySpellProjectile extends EntityThrowable {

    /** Default Psi colour used until CAD colorizers are backported. */
    protected static final int SPELL_COLOR = 0x13C5FF;

    private static final String TAG_CASTER_UUID = "casterUUID";
    private static final String TAG_TIME_ALIVE = "timeAlive";
    private static final String TAG_SPELL = "spell";

    protected Spell spell;
    protected CompiledSpell compiledSpell;
    private String casterUUID; // Store UUID as string for 1.7.10
    protected int timeAlive;

    // Required for entity registration
    public EntitySpellProjectile(World world) {
        super(world);
    }

    // Main constructor for creating projectiles
    public EntitySpellProjectile(World world, EntityLivingBase caster, Spell spell) {
        super(world, caster);
        this.spell = spell;
        this.casterUUID = caster.getUniqueID()
            .toString();

        // Pre-compile the spell for performance
        try {
            SpellCompiler compiler = new SpellCompiler();
            this.compiledSpell = compiler.compile(spell);
        } catch (SpellCompilationException e) {
            // If compilation fails, log it but don't crash
            System.err.println("[Psi] Failed to compile spell in projectile: " + e.getMessage());
            this.compiledSpell = null;
        }
    }

    @Override
    public void onUpdate() {
        tickProjectile();
    }

    protected void tickProjectile() {
        super.onUpdate();
        updateProjectileVisuals();
    }

    /** Keeps an embedded projectile alive and visual without Throwable's gravity update. */
    protected void tickStationaryProjectile() {
        lastTickPosX = prevPosX = posX;
        lastTickPosY = prevPosY = posY;
        lastTickPosZ = prevPosZ = posZ;
        ticksExisted++;
        updateProjectileVisuals();
    }

    private void updateProjectileVisuals() {
        timeAlive++;

        if (timeAlive > getLiveTime()) {
            setDead();
        }

        if (worldObj.isRemote && !isDead) spawnTrailParticles();
    }

    /** Lifetime in ticks; charge and mine bullets override this. */
    public int getLiveTime() {
        return 100;
    }

    /** Number of sparkle motes emitted each tick, matching the modern projectile family. */
    public int getParticleCount() {
        return 5;
    }

    protected void spawnTrailParticles() {
        double speed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        double baseX = speed < 1.0E-5D ? 0D : motionX / speed;
        double baseY = speed < 1.0E-5D ? 0D : motionY / speed;
        double baseZ = speed < 1.0E-5D ? 0D : motionZ / speed;
        double distance = 0.15D;
        if (this instanceof EntitySpellGrenade) {
            baseY += 1D;
            distance = 0.05D;
        }
        for (int i = 0; i < getParticleCount(); i++) {
            double vx = baseX + (rand.nextDouble() - 0.5D) * 0.6D;
            double vy = baseY + (rand.nextDouble() - 0.5D) * 0.6D;
            double vz = baseZ + (rand.nextDouble() - 0.5D) * 0.6D;
            double length = Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (length < 1.0E-5D) continue;
            float r = ((SPELL_COLOR >> 16) & 255) / 255F;
            float g = ((SPELL_COLOR >> 8) & 255) / 255F;
            float b = (SPELL_COLOR & 255) / 255F;
            Psi.proxy.sparkleFX(posX, posY, posZ, r, g, b,
                (float) (vx / length * distance), (float) (vy / length * distance), (float) (vz / length * distance), 1.2F, 12);
        }
    }

    @Override
    protected void onImpact(MovingObjectPosition mop) {
        // Don't execute on client side
        if (worldObj.isRemote) {
            return;
        }

        executeSpell(mop == null ? null : mop.entityHit);
        setDead();
    }

    protected void executeSpell(net.minecraft.entity.Entity hitEntity) {
        // Execute the spell if we have one
        EntityPlayer caster = getCaster();
        if (spell != null && compiledSpell != null && caster != null) {

            // Create spell context
            SpellContext context = new SpellContext();
            context.caster = caster;
            context.focalPoint = this; // Use the projectile as focal point
            context.spell = spell;

            // If we hit an entity, store it in customData for tricks to use
            if (hitEntity != null) {
                context.customData.put("psi:hitEntity", hitEntity);
            }

            // Execute the spell
            try {
                compiledSpell.execute(context);
            } catch (vazkii.psi.api.spell.SpellRuntimeException e) {
                caster.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        net.minecraft.util.EnumChatFormatting.RED + e.getTranslatedMessage()));
            } catch (Exception e) {
                System.err.println("[Psi] Error executing spell from projectile: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        nbt.setString(TAG_CASTER_UUID, casterUUID != null ? casterUUID : "");
        nbt.setInteger(TAG_TIME_ALIVE, timeAlive);

        // Save the spell
        if (spell != null) {
            NBTTagCompound spellNbt = new NBTTagCompound();
            spell.writeToNBT(spellNbt);
            nbt.setTag(TAG_SPELL, spellNbt);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        casterUUID = nbt.getString(TAG_CASTER_UUID);
        timeAlive = nbt.getInteger(TAG_TIME_ALIVE);

        // Load the spell
        if (nbt.hasKey(TAG_SPELL)) {
            try {
                spell = Spell.readFromNBT(nbt.getCompoundTag(TAG_SPELL));
                if (spell != null) {
                    SpellCompiler compiler = new SpellCompiler();
                    compiledSpell = compiler.compile(spell);
                }
            } catch (Exception e) {
                System.err.println("[Psi] Failed to load spell from projectile NBT: " + e.getMessage());
            }
        }
    }

    @Override
    protected float getGravityVelocity() {
        // No gravity - flies straight
        return 0.0F;
    }

    // Getters for accessing projectile data
    public Spell getSpell() {
        return spell;
    }

    public int getTimeAlive() {
        return timeAlive;
    }

    private EntityPlayer getCaster() {
        if (getThrower() instanceof EntityPlayer) return (EntityPlayer) getThrower();
        try {
            return casterUUID == null || casterUUID.isEmpty() ? null
                : worldObj.func_152378_a(java.util.UUID.fromString(casterUUID));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
