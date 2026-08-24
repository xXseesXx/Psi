/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

/**
 * Holder class for a spell's piece grid.
 * Barebones version for 1.7.10 - just the grid data structure.
 */
public class SpellGrid {

    public static final int GRID_SIZE = 9;
    public static final int GRID_CENTER = (GRID_SIZE - 1) / 2;

    public final Spell spell;
    public SpellPiece[][] gridData;

    public SpellGrid(Spell spell) {
        this.spell = spell;
        this.gridData = new SpellPiece[GRID_SIZE][GRID_SIZE];
    }

    /**
     * Check if coordinates are within the grid bounds.
     */
    public static boolean exists(int x, int y) {
        return x >= 0 && y >= 0 && x < GRID_SIZE && y < GRID_SIZE;
    }

    /**
     * Check if the grid is empty (no pieces).
     */
    public boolean isEmpty() {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (gridData[x][y] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the size of the spell (distance from leftmost to rightmost piece).
     * Used for bandwidth calculation.
     */
    public int getSize() {
        if (isEmpty()) {
            return 0;
        }

        int leftmost = GRID_SIZE;
        int rightmost = -1;
        int topmost = GRID_SIZE;
        int bottommost = -1;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (gridData[x][y] != null) {
                    if (x < leftmost) {
                        leftmost = x;
                    }
                    if (x > rightmost) {
                        rightmost = x;
                    }
                    if (y < topmost) {
                        topmost = y;
                    }
                    if (y > bottommost) {
                        bottommost = y;
                    }
                }
            }
        }

        return Math.max(rightmost - leftmost + 1, bottommost - topmost + 1);
    }
}
