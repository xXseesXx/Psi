/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/lib/ModTags.java:19
 * Modern uses net.minecraft.tags.TagKey<Item/Block> + ResourceLocation.fromNamespaceAndPath.
 * GTNH 1.7.10 adaptation: TagKey does not exist — we expose plain string tag names for use
 * with OreDictionary / manual recipe matching. Keeping the same constant names and prefix()
 * helpers maintains close-to-source call sites.
 * When a modern file references ModTags.PSIDUST, in 1.7.10 check
 * OreDictionary.getOres("psidust") or direct string comparison.
 */
package vazkii.psi.common.lib;

import net.minecraft.util.ResourceLocation;

import vazkii.psi.common.Psi;

public class ModTags {

    // Modern: TagKey<Item> — 1.7.10: string identifiers
    public static final String PSIDUST = "psidust";
    public static final String IVORY_SUBSTANCE = "ivory_substance";
    public static final String EBONY_SUBSTANCE = "ebony_substance";

    public static final String INGOT_PSIMETAL = "ingotPsimetal";
    public static final String BLOCK_PSIMETAL = "blockPsimetal";
    public static final String GEM_PSIGEM = "gemPsigem";
    public static final String BLOCK_PSIGEM = "blockPsigem";
    public static final String INGOT_EBONY_PSIMETAL = "ingotEbonyPsimetal";
    public static final String BLOCK_EBONY_PSIMETAL = "blockEbonyPsimetal";
    public static final String INGOT_IVORY_PSIMETAL = "ingotIvoryPsimetal";
    public static final String BLOCK_IVORY_PSIMETAL = "blockIvoryPsimetal";

    // Legacy TagKey helper preserved for documentation — now returns ResourceLocation
    public static ResourceLocation tagLocation(String name) {
        return prefix(name);
    }

    public static ResourceLocation forgeTagLocation(String name) {
        return new ResourceLocation("c", name);
    }

    public static ResourceLocation prefix(String path) {
        return Psi.location(path);
    }

    public static class Blocks {

        public static final String BLOCK_PSIMETAL = ModTags.BLOCK_PSIMETAL;
        public static final String BLOCK_PSIGEM = ModTags.BLOCK_PSIGEM;
        public static final String BLOCK_EBONY_PSIMETAL = ModTags.BLOCK_EBONY_PSIMETAL;
        public static final String BLOCK_IVORY_PSIMETAL = ModTags.BLOCK_IVORY_PSIMETAL;
    }
}
