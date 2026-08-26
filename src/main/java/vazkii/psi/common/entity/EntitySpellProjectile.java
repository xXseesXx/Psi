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

/**
 * A projectile entity that carries and executes a spell on impact.
 */
public class EntitySpellProjectile extends EntityThrowable {

    private static final String TAG_CASTER_UUID = "casterUUID";
    private static final String TAG_TIME_ALIVE = "timeAlive";
    private static final String TAG_SPELL = "spell";

    private Spell spell;
    private CompiledSpell compiledSpell;
    private String casterUUID; // Store UUID as string for 1.7.10
    private int timeAlive;

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
        super.onUpdate();

        timeAlive++;

        // Despawn after 5 seconds (100 ticks)
        if (timeAlive > 100) {
            setDead();
        }

        // TODO: Add particle trail in future phase
    }

    @Override
    protected void onImpact(MovingObjectPosition mop) {
        // Don't execute on client side
        if (worldObj.isRemote) {
            return;
        }

        // Execute the spell if we have one
        if (spell != null && compiledSpell != null && getThrower() instanceof EntityPlayer) {
            EntityPlayer caster = (EntityPlayer) getThrower();

            // Create spell context
            SpellContext context = new SpellContext();
            context.caster = caster;
            context.focalPoint = this; // Use the projectile as focal point
            context.spell = spell;

            // If we hit an entity, store it in customData for tricks to use
            if (mop.entityHit != null) {
                context.customData.put("psi:hitEntity", mop.entityHit);
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

        // Despawn the projectile
        setDead();
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
}
