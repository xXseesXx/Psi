package vazkii.psi.common.core.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;
import vazkii.psi.client.gui.GuiSpellProgrammer;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_SPELL_PROGRAMMER = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // No server-side GUI for spell programmer (client-only editing)
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_SPELL_PROGRAMMER) {
            // Get the CAD from player's held item
            ItemStack held = player.getHeldItem();
            if (held != null) {
                return new GuiSpellProgrammer(held);
            }
        }
        return null;
    }
}
