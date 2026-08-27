/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/spell/ISpellCache.java:18
 */
package vazkii.psi.api.spell;

public interface ISpellCache {

    CompiledSpell getCompiledSpell(Spell spell);
}
