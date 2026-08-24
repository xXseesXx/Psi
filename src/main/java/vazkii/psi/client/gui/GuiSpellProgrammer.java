package vazkii.psi.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.item.ItemCAD;

public class GuiSpellProgrammer extends GuiScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation("psi", "textures/gui/programmer.png");

    private final ItemStack cadStack;
    private Spell editingSpell;

    // GUI dimensions
    private int xSize = 256;
    private int ySize = 216;
    private int guiLeft;
    private int guiTop;

    public GuiSpellProgrammer(ItemStack cadStack) {
        this.cadStack = cadStack;

        // Load spell from CAD if it has one
        this.editingSpell = ItemCAD.getSpell(cadStack);
        if (this.editingSpell == null) {
            // Create empty spell if CAD has no spell
            this.editingSpell = new Spell();
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        // Center the GUI on screen
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw dark background
        this.drawDefaultBackground();

        // Set up OpenGL for texture rendering
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw GUI background (will use texture in Milestone 3)
        drawGuiBackground();

        // Draw title
        String title = "Spell Programmer";
        int titleX = guiLeft + (xSize - fontRendererObj.getStringWidth(title)) / 2;
        int titleY = guiTop + 6;
        fontRendererObj.drawString(title, titleX, titleY, 0x404040);

        // Draw spell name if editing existing spell
        if (editingSpell != null && editingSpell.name != null && !editingSpell.name.isEmpty()) {
            String spellName = "Editing: " + editingSpell.name;
            int nameX = guiLeft + 8;
            int nameY = guiTop + 20;
            fontRendererObj.drawString(spellName, nameX, nameY, 0x404040);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawGuiBackground() {
        // For Milestone 1, just draw a simple rectangle
        // In Milestone 3, we'll bind the programmer.png texture
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFC6C6C6);

        // Draw border
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + 1, 0xFF000000); // Top
        drawRect(guiLeft, guiTop + ySize - 1, guiLeft + xSize, guiTop + ySize, 0xFF000000); // Bottom
        drawRect(guiLeft, guiTop, guiLeft + 1, guiTop + ySize, 0xFF000000); // Left
        drawRect(guiLeft + xSize - 1, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF000000); // Right
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Don't pause in multiplayer
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // ESC key (keyCode 1) closes the GUI
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        // Mouse click handling will be added in later milestones
    }
}
