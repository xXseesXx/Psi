/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/internal/DummyPlayerData.java:22
 * GTNH adaptation: see IPlayerData.
 */
package vazkii.psi.api.internal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.spell.SpellPiece;

public class DummyPlayerData implements IPlayerData {

    @Override
    public int getTotalPsi() {
        return 0;
    }

    @Override
    public int getAvailablePsi() {
        return 0;
    }

    @Override
    public int getLastAvailablePsi() {
        return 0;
    }

    @Override
    public int getRegenCooldown() {
        return 0;
    }

    @Override
    public int getRegenPerTick() {
        return 0;
    }

    @Override
    public boolean isOverflowed() {
        return false;
    }

    @Override
    public void deductPsi(int psi, int cd, boolean sync, boolean shatter) {}

    @Override
    public boolean isPieceGroupUnlocked(ResourceLocation group, ResourceLocation piece) {
        return false;
    }

    @Override
    public void unlockPieceGroup(ResourceLocation group) {}

    @Override
    public void markPieceExecuted(SpellPiece piece) {}

    @Override
    public NBTTagCompound getCustomData() {
        return new NBTTagCompound();
    }

    @Override
    public void save() {}
}
