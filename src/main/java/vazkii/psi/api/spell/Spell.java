/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

/**
 * STUB for Phase 2: Minimal spell container.
 * Full implementation will be added in Phase 7 (spell piece linking).
 * 
 * 1.7.10 Backport: Barebones spell structure.
 */
public class Spell {

    /**
     * Size of the spell grid.
     */
    public static final int GRID_SIZE = 9;

    /**
     * Metadata for this spell (stats, flags).
     */
    public final SpellMetadata metadata = new SpellMetadata();

    /**
     * The spell grid (9x9 array of pieces).
     * Full implementation in Phase 7.
     */
    public SpellPiece[][] grid = new SpellPiece[GRID_SIZE][GRID_SIZE];

    /**
     * Name of this spell.
     */
    public String name = "";

    public Spell() {
        // Stub constructor
    }

}
