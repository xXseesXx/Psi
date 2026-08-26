package vazkii.psi.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.container.ContainerCADAssembler;
import vazkii.psi.common.item.ItemCAD;

public class GuiCADAssembler extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("psi", "textures/gui/cad_assembler.png");
    private static final String[] CAD_STATS = { "Efficiency", "Potency", "Complexity", "Projection", "Bandwidth",
        "Sockets", ItemCAD.STAT_MEMORY, "Overflow" };
    private static final String[] CAD_STAT_KEYS = { "efficiency", "potency", "complexity", "projection", "bandwidth",
        "sockets", "memory", "overflow" };

    public GuiCADAssembler(EntityPlayer player, TileCADAssembler assembler) {
        super(new ContainerCADAssembler(player, assembler));
        xSize = 256;
        ySize = 225;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = "CAD Assembler";
        fontRendererObj.drawString(name, (xSize - fontRendererObj.getStringWidth(name)) / 2, 10, 0x404040);

        ItemStack cad = ((ContainerCADAssembler) inventorySlots).assembler.getStackInSlot(TileCADAssembler.SLOT_OUTPUT);
        if (cad == null || !(cad.getItem() instanceof ItemCAD)) return;

        String title = EnumChatFormatting.BOLD + StatCollector.translateToLocal("psimisc.stats");
        fontRendererObj.drawStringWithShadow(title, 213 - fontRendererObj.getStringWidth(title) / 2, 32, 0xFFFFFF);
        for (int i = 0; i < CAD_STATS.length; i++) {
            String label = StatCollector.translateToLocal("psi.cadstat." + CAD_STAT_KEYS[i]);
            String stat = EnumChatFormatting.AQUA + label
                + EnumChatFormatting.RESET
                + ": "
                + ItemCAD.getStat(cad, CAD_STATS[i]);
            fontRendererObj.drawStringWithShadow(stat, 179, 45 + i * 10, 0xFFFFFF);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float ticks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        TileCADAssembler assembler = ((ContainerCADAssembler) inventorySlots).assembler;
        for (int i = 0; i < TileCADAssembler.MAGAZINE_SLOTS; i++) {
            if (!assembler.isBulletSlotEnabled(i)) {
                drawTexturedModalRect(guiLeft + 17 + i % 3 * 18, guiTop + 57 + i / 3 * 18, 16, ySize, 16, 16);
            }
        }
    }
}
