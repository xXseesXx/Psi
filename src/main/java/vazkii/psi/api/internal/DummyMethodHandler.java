/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/internal/DummyMethodHandler.java:28
 * GTNH adaptation: see IInternalMethodHandler.
 */
package vazkii.psi.api.internal;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.ISpellCache;
import vazkii.psi.api.spell.ISpellCompiler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;

public final class DummyMethodHandler implements IInternalMethodHandler {

    @Override
    public IPlayerData getDataForPlayer(EntityPlayer player) {
        return new DummyPlayerData();
    }

    @Override
    public ResourceLocation getProgrammerTexture() {
        return new ResourceLocation("psi", "textures/gui/programmer.png");
    }

    @Override
    public ISpellCompiler getCompiler() {
        return null;
    }

    @Override
    public ISpellCache getSpellCache() {
        return null;
    }

    @Override
    public void delayContext(SpellContext context) {}

    @Override
    public void setCrashData(CompiledSpell spell, SpellPiece piece) {}

    @Override
    public void renderTooltip(int x, int y, List<String> tooltipData, int color, int color2, int width, int height) {}

    @Override
    public ItemStack createDefaultCAD(List<ItemStack> components) {
        return null;
    }

    @Override
    public ItemStack createCAD(ItemStack base, List<ItemStack> components) {
        return null;
    }
}
