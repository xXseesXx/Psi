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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import vazkii.psi.api.internal.MathHelper;
import vazkii.psi.api.internal.Vector3;

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
     * The loopcast index of this context. This is always 0 when the spell is cast as not a
     * loopcast. Increments every time for each loopcast iteration.
     */
    public int loopcastIndex = 0;

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

    /**
     * Sets the spell and returns itself.
     * In full implementation, this would compile and cache.
     * For barebones, just stores the spell directly.
     */
    public SpellContext setSpell(Spell spell) {
        this.spell = spell;
        return this;
    }

    public SpellContext setLoopcastIndex(int i) {
        loopcastIndex = i;
        return this;
    }

    public boolean isValid() {
        return spell != null && caster != null && focalPoint != null;
    }

    public boolean shouldSuppressErrors() {
        return isValid() && spell.metadata.errorsSuppressed;
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
