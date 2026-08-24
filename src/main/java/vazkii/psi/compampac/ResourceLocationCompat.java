/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.compampac;

import net.minecraft.util.ResourceLocation;

/**
 * Compatibility wrapper for ResourceLocation creation.
 * 
 * Modern Minecraft (1.21+) uses ResourceLocation.fromNamespaceAndPath(namespace, path)
 * while 1.7.10 uses direct constructor new ResourceLocation(namespace, path).
 * 
 * This class provides a consistent API across versions.
 */
public class ResourceLocationCompat {

    /**
     * Creates a ResourceLocation from namespace and path.
     * Equivalent to modern: ResourceLocation.fromNamespaceAndPath(namespace, path)
     */
    public static ResourceLocation create(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    /**
     * Creates a ResourceLocation from a combined string "namespace:path".
     * If no colon is present, assumes "minecraft" namespace.
     * Equivalent to modern: ResourceLocation.fromNamespaceAndPath(combined)
     */
    public static ResourceLocation create(String combined) {
        return new ResourceLocation(combined);
    }

    /**
     * Creates a ResourceLocation for a Psi resource.
     * Convenience method for the Psi mod namespace.
     */
    public static ResourceLocation psi(String path) {
        return create("psi", path);
    }
}
