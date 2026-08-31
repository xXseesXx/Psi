/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.internal.MathHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.compat.BlockPosCompat;

/**
 * Context for a spell. Used for casting it.
 *
 * 1.7.10 Backport: Simplified for barebones functionality.
 * Removed: tool context, armor context, loopcast details, compiled spell cache.
 */
public final class SpellContext {

    /**
     * The maximum distance from the spell's {@link #focalPoint} a piece of the spell can interact with.<br>
     * This should be checked against in any tricks that affect parts of the world given a position
     * or entity.
     *
     * @see #isInRadius(Entity), {@link #isInRadius(Vector3)}, {@link #isInRadius(double, double, double)}
     */
    public static final double MAX_DISTANCE = 32;

    /**
     * A map for custom data where addon authors can put stuff. If you're going to put
     * anything here, prefix it with your mod ID to prevent collision. For example, Trick: Add Motion
     * uses psi:Entity1MotionX.
     */
    public final Map<String, Object> customData = new HashMap<>();

    // Runtime information ===============================================================

    /**
     * The player casting this spell.
     */
    public EntityPlayer caster;

    /**
     * The focal point of this spell. This can be the same as {@link #caster}, but will often be different,
     * like in cases where the spell is executed through a projectile bullet.
     */
    public Entity focalPoint;

    /**
     * The spell being executed.
     * Full spell/compilation system deferred to Phase 7.
     */
    public Spell spell;

    /**
     * Compiled spell (modern parity). In 1.7.10 barebones, cspell wraps spell for metadata/errorsSuppressed.
     * Modern counterpart: Psi-1.21.1/src/main/java/vazkii/psi/api/spell/SpellContext.java:61
     * Added to unblock tricks (Delay, ChangeSlot, SaveVector) pending full compiler expansion.
     */
    public CompiledSpell cspell;

    /**
     * The loopcast index of this context. This is always 0 when the spell is cast as not a
     * loopcast. Increments every time for each loopcast iteration.
     */
    public int loopcastIndex = 0;

    // Tool/Block/Actions parity with modern — added to unblock tricks (Delay, ChangeSlot)
    // Modern: ItemStack tool, BlockHitResult positionBroken, Stack<Action> actions, int delay, targetSlot etc
    public ItemStack tool = null;
    public BlockPosCompat positionBroken = null;
    public Stack<CompiledSpell.Action> actions = null;

    public int targetSlot = 1;
    public boolean shiftTargetSlot = true;
    public boolean customTargetSlot = false;

    public int delay = 0;

    // Armor/Sword parity — added for Attacker/AttackTarget/DamageTaken selectors (modern 230 lines)
    public EntityLivingBase attackedEntity = null;
    public EntityLivingBase attackingEntity = null;
    public double damageTaken = 0;

    /**
     * Grid storing evaluated values from spell pieces during execution.
     * Used by getParamValue() to retrieve results from parameter pieces.
     */
    public Object[][] evaluatedObjects = new Object[SpellGrid.GRID_SIZE][SpellGrid.GRID_SIZE];

    /**
     * Flag indicating if spell execution should stop.
     * Used by tricks that want to halt execution early.
     */
    public boolean stopped = false;

    // End Runtime information ===========================================================

    /**
     * Sets the {@link #caster} and returns itself. This also calls {@link #setFocalPoint(Entity)}.
     */
    public SpellContext setPlayer(EntityPlayer player) {
        caster = player;
        return setFocalPoint(player);
    }

    /**
     * Sets the focal point and returns itself.
     */
    public SpellContext setFocalPoint(Entity e) {
        focalPoint = e;
        return this;
    }

    public SpellContext setSpell(Spell spell) {
        this.spell = spell;
        try { this.cspell = spell == null ? null : new SpellCompiler().compile(spell); }
        catch (SpellCompilationException e) { this.cspell = null; }
        return this;
    }

    public SpellContext setCompiledSpell(CompiledSpell compiled) {
        this.cspell = compiled;
        this.spell = compiled != null ? compiled.sourceSpell : null;
        return this;
    }

    public SpellContext setLoopcastIndex(int i) {
        loopcastIndex = i;
        return this;
    }

    public int getTargetSlot() throws SpellRuntimeException {
        // GTNH: simplified — modern computes based on CAD slot + shift; 1.7.10 returns raw targetSlot
        return targetSlot;
    }

    public ItemStack getHarvestTool() throws SpellRuntimeException {
        if (tool != null && tool.stackSize > 0) return tool;
        ItemStack cad = vazkii.psi.api.PsiAPI.getPlayerCAD(caster);
        if (cad == null) throw new SpellRuntimeException(SpellRuntimeException.NO_CAD);
        return cad;
    }

    public boolean isValid() {
        return cspell != null;
    }

    public boolean shouldSuppressErrors() {
        if (!isValid()) return false;
        if (cspell != null && cspell.metadata != null) return cspell.metadata.errorsSuppressed;
        return spell != null && spell.metadata != null && spell.metadata.errorsSuppressed;
    }

    /**
     * Used to check if a vector is within this context's radius.
     *
     * @see #MAX_DISTANCE
     */
    public boolean isInRadius(Vector3 vec) {
        return isInRadius(vec.x, vec.y, vec.z);
    }

    /**
     * Used to check if an entity is within this context's radius.
     *
     * @see #MAX_DISTANCE
     */
    public boolean isInRadius(Entity e) {
        if (e == null) {
            return false;
        }
        if (e == focalPoint || e == caster) {
            return true;
        }

        return isInRadius(e.posX, e.posY, e.posZ);
    }

    /**
     * Used to check if an (x,y,z) position is within this context's radius.
     *
     * @see #MAX_DISTANCE
     */
    public boolean isInRadius(double x, double y, double z) {
        return MathHelper.pointDistanceSpace(x, y, z, focalPoint.posX, focalPoint.posY, focalPoint.posZ)
            <= MAX_DISTANCE;
    }

    /**
     * Verifies that an entity is valid and not immune to spells.
     * Throws SpellRuntimeException if invalid.
     */
    public void verifyEntity(Entity e) throws SpellRuntimeException {
        if (e == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        if (ISpellImmune.isImmune(e)) {
            throw new SpellRuntimeException(SpellRuntimeException.IMMUNE_TARGET);
        }
    }

}
