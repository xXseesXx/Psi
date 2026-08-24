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

    // GUI dimensions (from original Psi)
    private int xSize = 174;
    private int ySize = 184;
    private int guiLeft;
    private int guiTop;
    private int padLeft = 7;
    private int padTop = 7;
    private int gridLeft;
    private int gridTop;

    // Grid constants (Psi uses 9x9 grid)
    private static final int GRID_SIZE = 9;
    private static final int CELL_SIZE = 18;

    // Hover state
    private int cursorX = -1;
    private int cursorY = -1;

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

        // Center the GUI on screen (matching original)
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.gridLeft = guiLeft + padLeft;
        this.gridTop = guiTop + padTop;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw dark background
        this.drawDefaultBackground();

        // Set up OpenGL for texture rendering
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Bind and draw GUI background texture
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Calculate cursor position based on mouse
        cursorX = (mouseX - gridLeft) / CELL_SIZE;
        cursorY = (mouseY - gridTop) / CELL_SIZE;

        // Validate cursor is within grid bounds
        if (cursorX > 8 || cursorY > 8 || cursorX < 0 || cursorY < 0 || mouseX < gridLeft || mouseY < gridTop) {
            cursorX = -1;
            cursorY = -1;
        }

        // Draw hover highlight overlay from texture
        if (cursorX >= 0 && cursorY >= 0) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            // Hover overlay texture at (16, ySize) in programmer.png, size 16x16
            this.drawTexturedModalRect(
                gridLeft + cursorX * CELL_SIZE,
                gridTop + cursorY * CELL_SIZE,
                16,
                ySize,
                16,
                16);
        }

        // Draw spell pieces on grid (Milestone 4)
        drawSpellPieces();

        // Draw tooltips for hovered pieces
        if (cursorX >= 0 && cursorY >= 0) {
            vazkii.psi.api.spell.SpellPiece hoveredPiece = editingSpell.grid.gridData[cursorX][cursorY];
            if (hoveredPiece != null) {
                // Simple tooltip showing piece name from registry key
                String pieceName = hoveredPiece.registryKey.toString();
                // Make it more readable: "psi:trick_break_block" -> "Trick Break Block"
                String[] parts = pieceName.split(":");
                if (parts.length == 2) {
                    pieceName = parts[1].replace("_", " ");
                    // Capitalize first letter of each word
                    String[] words = pieceName.split(" ");
                    StringBuilder formatted = new StringBuilder();
                    for (String word : words) {
                        if (word.length() > 0) {
                            formatted.append(Character.toUpperCase(word.charAt(0)));
                            if (word.length() > 1) {
                                formatted.append(word.substring(1));
                            }
                            formatted.append(" ");
                        }
                    }
                    pieceName = formatted.toString()
                        .trim();
                }

                // Draw tooltip
                java.util.List<String> tooltip = new java.util.ArrayList<>();
                tooltip.add(pieceName);
                this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
            }
        }

        // Draw spell name if editing existing spell
        if (editingSpell != null && editingSpell.name != null && !editingSpell.name.isEmpty()) {
            String spellName = editingSpell.name;
            int nameX = guiLeft + xSize - 130;
            int nameY = guiTop + ySize - 14;
            fontRendererObj.drawString(spellName, nameX, nameY, 0x404040);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Draw all spell pieces currently on the grid.
     */
    private void drawSpellPieces() {
        if (editingSpell == null || editingSpell.grid == null) {
            return;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                vazkii.psi.api.spell.SpellPiece piece = editingSpell.grid.gridData[x][y];
                if (piece != null) {
                    drawPiece(piece, x, y);
                }
            }
        }
    }

    /**
     * Draw a single spell piece at the given grid coordinates.
     */
    private void drawPiece(vazkii.psi.api.spell.SpellPiece piece, int gridX, int gridY) {
        // Get the piece's texture based on its registry key
        // The registry key format is "psi:piece_name"
        String pieceName = piece.registryKey.getResourcePath();

        // In 1.7.10, texture paths don't include "textures/" prefix in ResourceLocation
        // The path should be: "psi:spell/piece_name.png"
        // which resolves to: assets/psi/textures/spell/piece_name.png
        ResourceLocation pieceTexture = new ResourceLocation(
            piece.registryKey.getResourceDomain(),
            "textures/spell/" + pieceName + ".png");

        // Calculate screen position
        int screenX = gridLeft + gridX * CELL_SIZE;
        int screenY = gridTop + gridY * CELL_SIZE;

        try {
            // Bind the piece texture
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager()
                .bindTexture(pieceTexture);

            // Draw the 16x16 piece icon (centered in 18x18 cell, so offset by 1px)
            this.drawTexturedModalRect(screenX + 1, screenY + 1, 0, 0, 16, 16);

        } catch (Exception e) {
            // If texture not found, draw a gray placeholder square with piece name initial
            drawRect(screenX + 1, screenY + 1, screenX + 17, screenY + 17, 0xFF808080);

            // Draw first letter of piece name for debugging
            if (pieceName.length() > 0) {
                String initial = String.valueOf(pieceName.charAt(0))
                    .toUpperCase();
                fontRendererObj.drawString(initial, screenX + 6, screenY + 5, 0xFFFFFF);
            }
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
