package vazkii.psi.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    private static final String TAG_CASTER_UUID = "casterUUID";
    private static final String TAG_TIME_ALIVE = "timeAlive";
    private static final String TAG_SPELL = "spell";
    private static final String TAG_COLORIZER = "colorizer";
    private static final int DW_COLORIZER = 20;

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

    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObjectByDataType(DW_COLORIZER, 5);
        dataWatcher.updateObject(DW_COLORIZER, new ItemStack(Blocks.air));
    }

    public EntitySpellProjectile setColorizer(ItemStack colorizer) {
        ItemStack stored = colorizer == null ? new ItemStack(Blocks.air) : colorizer.copy();
        stored.stackSize = 1;
        dataWatcher.updateObject(DW_COLORIZER, stored);
        return this;
    }

    public ItemStack getColorizer() {
        ItemStack stack = dataWatcher.getWatchableObjectItemStack(DW_COLORIZER);
        return stack != null && stack.getItem() != null && stack.getItem() != Item.getItemFromBlock(Blocks.air) ? stack
            : null;
    }

    public int getSpellColor() {
        return Psi.proxy.getColorForColorizer(getColorizer());
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
            int color = getSpellColor();
            float r = ((color >> 16) & 255) / 255F;
            float g = ((color >> 8) & 255) / 255F;
            float b = (color & 255) / 255F;
            Psi.proxy.sparkleFX(
                posX,
                posY,
                posZ,
                r,
                g,
                b,
                (float) (vx / length * distance),
                (float) (vy / length * distance),
                (float) (vz / length * distance),
                1.2F,
                12);
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
            context.setCompiledSpell(compiledSpell);

            // If we hit an entity, store it in customData for tricks to use
            if (hitEntity != null) {
                context.customData.put("psi:hitEntity", hitEntity);
            }

            compiledSpell.safeExecute(context);
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
        ItemStack colorizer = getColorizer();
        if (colorizer != null) nbt.setTag(TAG_COLORIZER, colorizer.writeToNBT(new NBTTagCompound()));
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
        if (nbt.hasKey(TAG_COLORIZER)) setColorizer(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(TAG_COLORIZER)));
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
