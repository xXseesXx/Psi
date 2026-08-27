/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/internal/IInternalMethodHandler.java:24
 * Modern uses net.minecraft.world.entity.player.Player, GuiGraphics, RenderType, Component, Dist.
 * GTNH adaptation: EntityPlayer, ResourceLocation, plain List<String> tooltips, no @OnlyIn.
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

public interface IInternalMethodHandler {

    IPlayerData getDataForPlayer(EntityPlayer player);

    ResourceLocation getProgrammerTexture();

    ISpellCompiler getCompiler();

    ISpellCache getSpellCache();

    void delayContext(SpellContext context);

    void setCrashData(CompiledSpell spell, SpellPiece piece);

    /**
     * 1.21.1: void renderTooltip(GuiGraphics, x,y, List<Component>, color,...)
     * 1.7.10: simplified — draw tooltip lines via Gui logic.
     */
    void renderTooltip(int x, int y, List<String> tooltipData, int color, int color2, int width, int height);

    ItemStack createDefaultCAD(List<ItemStack> components);

    ItemStack createCAD(ItemStack base, List<ItemStack> components);
}
