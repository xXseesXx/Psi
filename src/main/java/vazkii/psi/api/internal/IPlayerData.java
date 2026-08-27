/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/internal/IPlayerData.java:18
 * Modern uses net.minecraft.nbt.CompoundTag + ResourceLocation (1.21). GTNH adaptation:
 * CompoundTag -> NBTTagCompound, ResourceLocation -> net.minecraft.util.ResourceLocation
 */
package vazkii.psi.api.internal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.spell.SpellPiece;

public interface IPlayerData {

    int getTotalPsi();

    int getAvailablePsi();

    int getLastAvailablePsi();

    int getRegenCooldown();

    int getRegenPerTick();

    boolean isOverflowed();

    void deductPsi(int psi, int cd, boolean sync, boolean shatter);

    default boolean isPieceGroupUnlocked(ResourceLocation group) {
        return isPieceGroupUnlocked(group, null);
    }

    boolean isPieceGroupUnlocked(ResourceLocation group, ResourceLocation piece);

    void unlockPieceGroup(ResourceLocation group);

    void markPieceExecuted(SpellPiece piece);

    NBTTagCompound getCustomData();

    void save();
}
