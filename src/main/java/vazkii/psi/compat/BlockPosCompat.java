/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.compat;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Compatibility class for BlockPos, which doesn't exist in 1.7.10.
 * This is an immutable class representing a block position in 3D space.
 * In modern Minecraft (1.8+), BlockPos is a core class. For 1.7.10, we recreate
 * its functionality here.
 */
public class BlockPosCompat {

    public final int x;
    public final int y;
    public final int z;

    /**
     * Creates a new block position
     */
    public BlockPosCompat(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns a new BlockPos offset by the given amounts
     */
    public BlockPosCompat offset(int dx, int dy, int dz) {
        return new BlockPosCompat(x + dx, y + dy, z + dz);
    }

    /**
     * Returns a new BlockPos offset by the given direction
     */
    public BlockPosCompat offset(ForgeDirection direction) {
        return offset(direction.offsetX, direction.offsetY, direction.offsetZ);
    }

    /**
     * Returns a new BlockPos offset by the given direction and distance
     */
    public BlockPosCompat offset(ForgeDirection direction, int distance) {
        return offset(direction.offsetX * distance, direction.offsetY * distance, direction.offsetZ * distance);
    }

    /**
     * Returns a new BlockPos one block up
     */
    public BlockPosCompat up() {
        return up(1);
    }

    /**
     * Returns a new BlockPos n blocks up
     */
    public BlockPosCompat up(int n) {
        return offset(0, n, 0);
    }

    /**
     * Returns a new BlockPos one block down
     */
    public BlockPosCompat down() {
        return down(1);
    }

    /**
     * Returns a new BlockPos n blocks down
     */
    public BlockPosCompat down(int n) {
        return offset(0, -n, 0);
    }

    /**
     * Returns a new BlockPos one block north (negative Z)
     */
    public BlockPosCompat north() {
        return north(1);
    }

    /**
     * Returns a new BlockPos n blocks north
     */
    public BlockPosCompat north(int n) {
        return offset(0, 0, -n);
    }

    /**
     * Returns a new BlockPos one block south (positive Z)
     */
    public BlockPosCompat south() {
        return south(1);
    }

    /**
     * Returns a new BlockPos n blocks south
     */
    public BlockPosCompat south(int n) {
        return offset(0, 0, n);
    }

    /**
     * Returns a new BlockPos one block west (negative X)
     */
    public BlockPosCompat west() {
        return west(1);
    }

    /**
     * Returns a new BlockPos n blocks west
     */
    public BlockPosCompat west(int n) {
        return offset(-n, 0, 0);
    }

    /**
     * Returns a new BlockPos one block east (positive X)
     */
    public BlockPosCompat east() {
        return east(1);
    }

    /**
     * Returns a new BlockPos n blocks east
     */
    public BlockPosCompat east(int n) {
        return offset(n, 0, 0);
    }

    /**
     * Calculates Manhattan distance to another position
     */
    public int distanceManhattan(BlockPosCompat other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y) + Math.abs(z - other.z);
    }

    /**
     * Calculates squared Euclidean distance to another position
     */
    public double distanceSquared(BlockPosCompat other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates Euclidean distance to another position
     */
    public double distance(BlockPosCompat other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlockPosCompat other)) {
            return false;
        }
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        // Hash code compatible with modern BlockPos
        return (y + z * 31) * 31 + x;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    /**
     * Returns the X coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the Y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the Z coordinate
     */
    public int getZ() {
        return z;
    }
}
