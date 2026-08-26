package vazkii.psi.common.crafting.recipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.common.item.ItemCAD;

/**
 * Shapeless CAD + colorizer swap. The old colorizer is returned to the player
 * because 1.7.10 recipes have no remaining-item API.
 */
public class ColorizerChangeRecipe implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return find(inv, true) != null && find(inv, false) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack cad = find(inv, true);
        ItemStack colorizer = find(inv, false);
        if (cad == null || colorizer == null) return null;
        ItemStack copy = cad.copy();
        copy.stackSize = 1;
        ItemCAD.setColorizer(copy, colorizer);
        return copy;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    @SubscribeEvent
    public void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting == null || !(event.crafting.getItem() instanceof ItemCAD)) return;
        ItemStack cadIn = find(event.craftMatrix, true);
        ItemStack colorizer = find(event.craftMatrix, false);
        if (cadIn == null || colorizer == null) return;
        ItemStack old = ItemCAD.getColorizer(cadIn);
        if (old != null) give(event.player, old);
    }

    private static ItemStack find(IInventory inv, boolean cad) {
        ItemStack found = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) continue;
            boolean isCad = stack.getItem() instanceof ItemCAD;
            boolean isColorizer = stack.getItem() instanceof ICADColorizer;
            if (cad ? isCad : isColorizer) {
                if (found != null) return null;
                found = stack;
            } else if (!isCad && !isColorizer) return null;
        }
        return found;
    }

    private static void give(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) player.dropPlayerItemWithRandomChoice(stack, false);
    }
}
