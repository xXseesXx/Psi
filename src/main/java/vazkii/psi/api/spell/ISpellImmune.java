/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.IBossDisplayData;

/**
 * An entity that implements this is immune to psi, and as such can't be
 * affected by any Tricks. Check against this before executing any Tricks that
 * target entities.
 * 
 * 1.7.10 Backport: Simplified implementation for barebones functionality.
 */
public interface ISpellImmune {

    /**
     * Checks if an entity is immune to spells.
     * Currently only checks for boss entities (IBossDisplayData).
     */
    static boolean isImmune(Entity e) {
        if (e == null) {
            return false;
        }

        // Bosses are immune
        if (e instanceof IBossDisplayData) {
            return true;
        }

        // Entities implementing this interface are immune
        if (e instanceof ISpellImmune) {
            return true;
        }

        return false;
    }

}
