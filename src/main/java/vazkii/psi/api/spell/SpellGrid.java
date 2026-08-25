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

    /**
     * Get the piece at the specified side of the given position, or null if out of bounds or empty.
     */
    public SpellPiece getPieceAtSideSafely(int x, int y, SpellParam.Side side) {
        int xp = x + side.offx;
        int yp = y + side.offy;
        if (!exists(xp, yp)) {
            return null;
        }
        return gridData[xp][yp];
    }

    /** Move every piece one cell, without losing a spell at the grid edge. */
    public boolean shift(SpellParam.Side side, boolean doit) {
        int minX = GRID_SIZE, maxX = -1, minY = GRID_SIZE, maxY = -1;
        for (int x = 0; x < GRID_SIZE; x++) for (int y = 0; y < GRID_SIZE; y++) if (gridData[x][y] != null) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if (maxX < 0 || !exists(minX + side.offx, minY + side.offy) || !exists(maxX + side.offx, maxY + side.offy))
            return false;
        if (!doit) return true;
        SpellPiece[][] shifted = new SpellPiece[GRID_SIZE][GRID_SIZE];
        for (int x = 0; x < GRID_SIZE; x++) for (int y = 0; y < GRID_SIZE; y++) {
            SpellPiece piece = gridData[x][y];
            if (piece != null) {
                piece.x = x + side.offx;
                piece.y = y + side.offy;
                shifted[piece.x][piece.y] = piece;
            }
        }
        gridData = shifted;
        return true;
    }

    public void mirrorVertical() {
        transform(false, false);
    }

    public void rotate(boolean clockwise) {
        transform(true, clockwise);
    }

    private void transform(boolean rotate, boolean clockwise) {
        SpellPiece[][] transformed = new SpellPiece[GRID_SIZE][GRID_SIZE];
        for (int x = 0; x < GRID_SIZE; x++) for (int y = 0; y < GRID_SIZE; y++) {
            SpellPiece piece = gridData[x][y];
            if (piece == null) continue;
            int nx = rotate ? (clockwise ? GRID_SIZE - 1 - y : y) : x;
            int ny = rotate ? (clockwise ? x : GRID_SIZE - 1 - x) : GRID_SIZE - 1 - y;
            piece.x = nx;
            piece.y = ny;
            for (SpellParam<?> param : piece.paramSides.keySet()) {
                SpellParam.Side side = piece.paramSides.get(param);
                piece.paramSides.put(param, rotate ? rotateSide(side, clockwise) : mirrorSide(side));
            }
            transformed[nx][ny] = piece;
        }
        gridData = transformed;
    }

    private SpellParam.Side mirrorSide(SpellParam.Side side) {
        return side == SpellParam.Side.TOP ? SpellParam.Side.BOTTOM
            : side == SpellParam.Side.BOTTOM ? SpellParam.Side.TOP : side;
    }

    private SpellParam.Side rotateSide(SpellParam.Side side, boolean clockwise) {
        if (side == SpellParam.Side.OFF) return side;
        if (clockwise) return side == SpellParam.Side.TOP ? SpellParam.Side.RIGHT
            : side == SpellParam.Side.RIGHT ? SpellParam.Side.BOTTOM
                : side == SpellParam.Side.BOTTOM ? SpellParam.Side.LEFT : SpellParam.Side.TOP;
        return side == SpellParam.Side.TOP ? SpellParam.Side.LEFT
            : side == SpellParam.Side.LEFT ? SpellParam.Side.BOTTOM
                : side == SpellParam.Side.BOTTOM ? SpellParam.Side.RIGHT : SpellParam.Side.TOP;
    }
}
