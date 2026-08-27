/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/spell/ISpellCompiler.java:18
 * Modern uses Either<CompiledSpell, SpellCompilationException> (DFU). GTNH 1.7.10:
 * No DFU — compiler throws SpellCompilationException directly or returns CompiledSpell.
 * Keep interface for close-to-source call sites; backport SpellCompiler implements this.
 */
package vazkii.psi.api.spell;

public interface ISpellCompiler {

    CompiledSpell compile(Spell in) throws SpellCompilationException;
}
