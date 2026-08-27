/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/PsiAPI.java:38
 * Modern uses NeoForge EntityCapability/ItemCapability, RegistryBuilder, SimpleTier.
 * GTNH adaptation: Capabilities do not exist — exposed as string keys for use with
 * IExtendedEntityProperties (player) and ItemStack NBT (CAD). Registry is replaced
 * by vazkii.psi.common.spell.SpellPieceRegistry HashMap. Tool material uses
 * net.minecraftforge.common.util.EnumHelper.
 * Keep field names identical so diff with modern stays minimal.
 */
package vazkii.psi.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.DummyMethodHandler;
import vazkii.psi.api.internal.IInternalMethodHandler;
import vazkii.psi.common.Psi;

public final class PsiAPI {

    public static final String MOD_ID = "psi";

    // Modern: EntityCapability<ISpellImmune, Void> — 1.7.10: ExtendedEntityProperties key
    public static final String SPELL_IMMUNE_CAPABILITY = "psi:spell_immune";
    public static final String DETONATION_HANDLER_CAPABILITY = "psi:detonation_handler";
    // Modern: ItemCapability<IPsiBarDisplay> — 1.7.10: capability simulated via IHUDItem / CADData
    public static final String PSI_BAR_DISPLAY_CAPABILITY = "psi:psi_bar_display";
    public static final String SPELL_ACCEPTOR_CAPABILITY = "psi:spell_acceptor";
    public static final String CAD_DATA_CAPABILITY = "psi:cad_data";
    public static final String SOCKETABLE_CAPABILITY = "psi:socketable";

    // Modern: ResourceKey<Registry<Class<? extends SpellPiece>>> — 1.7.10: SpellPieceRegistry
    public static final String SPELL_PIECE_REGISTRY_TYPE_KEY = "psi:spell_piece_registry_type_key";
    public static final String ADVANCEMENT_GROUP_REGISTRY_KEY = "psi:advancement_group_registry_key";

    /**
     * Tool material for Psimetal tools — mirrors modern SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, ...).
     * Initialized via EnumHelper in CommonProxy preInit; null until then.
     */
    public static net.minecraft.item.Item.ToolMaterial PSIMETAL_TOOL_MATERIAL = null;

    public static IInternalMethodHandler internalHandler = new DummyMethodHandler();

    /**
     * Gets the CAD the player is carrying. Mirrors modern PsiAPI.getPlayerCAD(Player).
     * Modern counterpart: PsiAPI.java:68
     */
    public static ItemStack getPlayerCAD(EntityPlayer player) {
        if (player == null) return null;
        ItemStack cad = null;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stackAt = player.inventory.getStackInSlot(i);
            if (stackAt != null && stackAt.getItem() instanceof ICAD) {
                if (cad != null) return null; // more than one CAD
                cad = stackAt;
            }
        }
        return cad;
    }

    public static int getPlayerCADSlot(EntityPlayer player) {
        if (player == null) return -1;
        int slot = -1;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stackAt = player.inventory.getStackInSlot(i);
            if (stackAt != null && stackAt.getItem() instanceof ICAD) {
                if (slot != -1) return -1;
                slot = i;
            }
        }
        return slot;
    }

    public static boolean canCADBeUpdated(EntityPlayer player) {
        if (player == null) return false;
        if (player.openContainer == null) return true;
        int cadSlot = getPlayerCADSlot(player);
        return cadSlot < 9 || cadSlot == 40;
    }

    public static ResourceLocation location(String path) {
        return Psi.location(path);
    }
}
