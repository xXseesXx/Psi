/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.internal;

/**
 * Math helper utilities for Psi.
 * 
 * 1.7.10 Backport: Minimal implementation for barebones functionality.
 */
public final class MathHelper {

    /**
     * Calculates the 3D Euclidean distance between two points.
     */
    public static double pointDistanceSpace(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
