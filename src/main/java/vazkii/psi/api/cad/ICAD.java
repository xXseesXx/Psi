package vazkii.psi.api.cad;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellRuntimeException;
//import vazkii.psi.api.spell.piece.PieceCraftingTrick;

/**
 * Base interface for a CAD.
 *
 * 1.7.10 port of the modern ICAD contract.
 */
public interface ICAD {

    ItemStack getComponentInSlot(ItemStack stack, EnumCADComponent type);

    int getStatValue(ItemStack stack, EnumCADStat stat);

    int getStoredPsi(ItemStack stack);

    void regenPsi(ItemStack stack, int psi);

    int consumePsi(ItemStack stack, int psi);

    int getMemorySize(ItemStack stack);

    void setStoredVector(ItemStack stack, int memorySlot, Vector3 vec)
            throws SpellRuntimeException;

    Vector3 getStoredVector(ItemStack stack, int memorySlot)
            throws SpellRuntimeException;

    int getTime(ItemStack stack);

    void incrementTime(ItemStack stack);

    int getSpellColor(ItemStack stack);

    //boolean craft(ItemStack cad, EntityPlayer player, PieceCraftingTrick trick);
}
