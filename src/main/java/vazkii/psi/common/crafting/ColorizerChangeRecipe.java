package vazkii.psi.common.crafting;

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
 * Crafting-table swap: one CAD plus one colorizer replaces the installed colour.
 * The previous colorizer is returned through the crafting event. Vanilla 1.7.10
 * recipes do not provide a remaining-items hook.
 */
public class ColorizerChangeRecipe implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return find(inv, false) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return find(inv, true);
    }

    private static ItemStack find(IInventory inv, boolean assemble) {
        ItemStack colorizer = null;
        ItemStack cad = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemCAD) {
                if (cad != null) return null;
                cad = stack;
            } else if (stack.getItem() instanceof ICADColorizer) {
                if (colorizer != null) return null;
                colorizer = stack;
            } else return null;
        }
        if (cad == null || colorizer == null) return null;
        if (!assemble) return cad;
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
        if (find(event.craftMatrix, false) == null) return;
        ItemStack cad = findCad(event.craftMatrix);
        if (cad == null) return;
        ItemStack previous = ItemCAD.getColorizer(cad);
        if (previous == null) return;
        if (!event.player.inventory.addItemStackToInventory(previous))
            event.player.dropPlayerItemWithRandomChoice(previous, false);
    }

    private static ItemStack findCad(IInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemCAD) return stack;
        }
        return null;
    }
}
