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

    // Grid constants (Psi uses 9x9 grid)
    private static final int GRID_SIZE = 9;
    private static final int CELL_SIZE = 18;
    private int gridOffsetX = 40; // Offset from left of GUI
    private int gridOffsetY = 40; // Offset from top of GUI

    // Hover state
    private int hoveredGridX = -1;
    private int hoveredGridY = -1;

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

        // Update hover state based on mouse position
        updateHoverState(mouseX, mouseY);

        // Draw the 9x9 grid
        drawGrid();

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

    /**
     * Update which grid cell the mouse is hovering over.
     */
    private void updateHoverState(int mouseX, int mouseY) {
        int gridStartX = guiLeft + gridOffsetX;
        int gridStartY = guiTop + gridOffsetY;
        int gridEndX = gridStartX + (GRID_SIZE * CELL_SIZE);
        int gridEndY = gridStartY + (GRID_SIZE * CELL_SIZE);

        // Check if mouse is within grid bounds
        if (mouseX >= gridStartX && mouseX < gridEndX && mouseY >= gridStartY && mouseY < gridEndY) {
            // Calculate which cell is hovered
            hoveredGridX = (mouseX - gridStartX) / CELL_SIZE;
            hoveredGridY = (mouseY - gridStartY) / CELL_SIZE;
        } else {
            // Not hovering over grid
            hoveredGridX = -1;
            hoveredGridY = -1;
        }
    }

    /**
     * Draw the 9x9 spell programming grid.
     */
    private void drawGrid() {
        int gridStartX = guiLeft + gridOffsetX;
        int gridStartY = guiTop + gridOffsetY;

        // Draw each cell
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                int cellX = gridStartX + (x * CELL_SIZE);
                int cellY = gridStartY + (y * CELL_SIZE);

                // Determine cell color
                int cellColor;
                if (x == hoveredGridX && y == hoveredGridY) {
                    // Hovered cell - light blue highlight
                    cellColor = 0xFFADD8E6;
                } else {
                    // Normal cell - white
                    cellColor = 0xFFFFFFFF;
                }

                // Draw cell background
                drawRect(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, cellColor);

                // Draw cell border (dark gray)
                drawRect(cellX, cellY, cellX + CELL_SIZE, cellY + 1, 0xFF808080); // Top
                drawRect(cellX, cellY + CELL_SIZE - 1, cellX + CELL_SIZE, cellY + CELL_SIZE, 0xFF808080); // Bottom
                drawRect(cellX, cellY, cellX + 1, cellY + CELL_SIZE, 0xFF808080); // Left
                drawRect(cellX + CELL_SIZE - 1, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0xFF808080); // Right
            }
        }

        // Draw grid label
        String gridLabel = "Spell Grid (9x9)";
        int labelX = gridStartX;
        int labelY = gridStartY - 12;
        fontRendererObj.drawString(gridLabel, labelX, labelY, 0x404040);

        // Show hovered coordinates for debugging
        if (hoveredGridX >= 0 && hoveredGridY >= 0) {
            String coordText = "Cell: (" + hoveredGridX + ", " + hoveredGridY + ")";
            int coordX = gridStartX + (GRID_SIZE * CELL_SIZE) - fontRendererObj.getStringWidth(coordText);
            int coordY = gridStartY - 12;
            fontRendererObj.drawString(coordText, coordX, coordY, 0x606060);
        }
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
