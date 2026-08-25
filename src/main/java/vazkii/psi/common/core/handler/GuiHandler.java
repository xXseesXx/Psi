package vazkii.psi.common.core.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;
import vazkii.psi.client.gui.GuiSpellProgrammer;
import vazkii.psi.client.gui.GuiCADAssembler;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.block.tile.container.ContainerCADAssembler;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_SPELL_PROGRAMMER = 0;
    public static final int GUI_CAD_ASSEMBLER = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_CAD_ASSEMBLER && world.getTileEntity(x, y, z) instanceof TileCADAssembler) {
            return new ContainerCADAssembler(player, (TileCADAssembler) world.getTileEntity(x, y, z));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_SPELL_PROGRAMMER) {
            if (world.getTileEntity(x, y, z) instanceof TileProgrammer) return new GuiSpellProgrammer((TileProgrammer) world.getTileEntity(x, y, z));
            // Preserve the dev CAD's standalone editor without making it part of the assembler/magazine path.
            ItemStack held = player.getHeldItem();
            if (held != null) return new GuiSpellProgrammer(held);
        }
        if (ID == GUI_CAD_ASSEMBLER && world.getTileEntity(x, y, z) instanceof TileCADAssembler) {
            return new GuiCADAssembler(player, (TileCADAssembler) world.getTileEntity(x, y, z));
        }
        return null;
    }
}
