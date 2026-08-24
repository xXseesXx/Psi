/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

/**
 * STUB for Phase 1: Minimal interface to allow SpellParam.canAccept() to compile.
 * Full implementation will be added in Phase 2.
 */
public interface SpellPiece {

    /**
     * Gets the evaluation type of this piece (e.g., Double.class, Vector3.class, Entity.class).
     */
    Class<?> getEvaluationType();

    /**
     * Gets the piece type (TRICK, SELECTOR, OPERATOR, etc.).
     */
    EnumPieceType getPieceType();

}
