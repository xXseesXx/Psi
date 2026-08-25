package vazkii.psi.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.block.tile.container.ContainerCADAssembler;

public class GuiCADAssembler extends GuiContainer {
    private static final ResourceLocation TEXTURE=new ResourceLocation("psi","textures/gui/cad_assembler.png");
    public GuiCADAssembler(EntityPlayer player, TileCADAssembler assembler) { super(new ContainerCADAssembler(player,assembler)); xSize=256; ySize=225; }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY) { String name="CAD Assembler"; fontRendererObj.drawString(name,(xSize-fontRendererObj.getStringWidth(name))/2,10,0x404040); }
    @Override protected void drawGuiContainerBackgroundLayer(float ticks,int mouseX,int mouseY) { GL11.glColor4f(1F,1F,1F,1F); mc.getTextureManager().bindTexture(TEXTURE); drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize); if(((ContainerCADAssembler)inventorySlots).assembler.getStackInSlot(0)==null) for(int i=0;i<TileCADAssembler.MAGAZINE_SLOTS;i++) drawTexturedModalRect(guiLeft+17+i%3*18,guiTop+57+i/3*18,16,ySize,16,16); }
}
