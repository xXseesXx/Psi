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

    // Piece selection state
    private boolean pieceSelectionOpen = false;
    private int selectionTargetX = -1;
    private int selectionTargetY = -1;

    // All available piece types for selection (our 9 basic pieces)
    private static final String[] AVAILABLE_PIECES = { "psi:constant_number", "psi:constant_string", "psi:operator_sum",
        "psi:selector_caster", "psi:selector_raycast", "psi:selector_entity_position", "psi:trick_debug",
        "psi:trick_break_block", "psi:trick_explode" };

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

        // Draw piece selection overlay LAST (on top of everything)
        if (pieceSelectionOpen) {
            drawPieceSelectionOverlay(mouseX, mouseY);
        }
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
        // ESC key (keyCode 1) closes the GUI or piece selection
        if (keyCode == 1) {
            if (pieceSelectionOpen) {
                closePieceSelection();
            } else {
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
            }
        }
    }

    /**
     * Open the piece selection overlay at the given grid coordinates.
     */
    private void openPieceSelection(int gridX, int gridY) {
        pieceSelectionOpen = true;
        selectionTargetX = gridX;
        selectionTargetY = gridY;
    }

    /**
     * Close the piece selection overlay.
     */
    private void closePieceSelection() {
        pieceSelectionOpen = false;
        selectionTargetX = -1;
        selectionTargetY = -1;
    }

    /**
     * Draw the piece selection overlay.
     */
    private void drawPieceSelectionOverlay(int mouseX, int mouseY) {
        // Semi-transparent dark background over entire screen
        drawRect(0, 0, this.width, this.height, 0x88000000);

        // Calculate panel dimensions (3x3 grid of 20x20 buttons with spacing)
        int panelWidth = 3 * 20 + 4 * 4; // 3 buttons, 4 gaps
        int panelHeight = 3 * 20 + 4 * 4;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        // Draw panel background
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF404040);
        drawRect(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF202020);

        // Draw title
        String title = "Select Piece";
        int titleX = panelX + (panelWidth - fontRendererObj.getStringWidth(title)) / 2;
        fontRendererObj.drawString(title, titleX, panelY - 12, 0xFFFFFF);

        // Draw 9 pieces in 3x3 grid
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 0; i < AVAILABLE_PIECES.length && i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int btnX = panelX + 4 + col * 24;
            int btnY = panelY + 4 + row * 24;

            // Highlight on hover
            boolean hovered = mouseX >= btnX && mouseX < btnX + 20 && mouseY >= btnY && mouseY < btnY + 20;

            // Button background
            int bgColor = hovered ? 0xFF5555FF : 0xFF333333;
            drawRect(btnX, btnY, btnX + 20, btnY + 20, bgColor);
            drawRect(btnX + 1, btnY + 1, btnX + 19, btnY + 19, 0xFF000000);

            // Draw piece icon (16x16 centered in 20x20 button)
            String pieceName = AVAILABLE_PIECES[i].split(":")[1];
            ResourceLocation pieceTexture = new ResourceLocation("psi", "textures/spell/" + pieceName + ".png");

            try {
                this.mc.getTextureManager()
                    .bindTexture(pieceTexture);
                this.drawTexturedModalRect(btnX + 2, btnY + 2, 0, 0, 16, 16);
            } catch (Exception e) {
                // Fallback: gray square
                drawRect(btnX + 2, btnY + 2, btnX + 18, btnY + 18, 0xFF808080);
            }

            // Draw tooltip on hover
            if (hovered) {
                String tooltip = formatPieceName(AVAILABLE_PIECES[i]);
                java.util.List<String> tooltipList = new java.util.ArrayList<>();
                tooltipList.add(tooltip);
                this.drawHoveringText(tooltipList, mouseX, mouseY, fontRendererObj);
            }
        }
    }

    /**
     * Handle mouse clicks in the piece selection overlay.
     */
    private void handlePieceSelectionClick(int mouseX, int mouseY, int button) {
        // Only handle left-clicks
        if (button != 0) {
            closePieceSelection();
            return;
        }

        // Calculate panel position
        int panelWidth = 3 * 20 + 4 * 4;
        int panelHeight = 3 * 20 + 4 * 4;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        // Check if click is inside panel
        boolean insidePanel = mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= panelY
            && mouseY < panelY + panelHeight;

        if (!insidePanel) {
            closePieceSelection();
            return;
        }

        // Check which piece button was clicked
        for (int i = 0; i < AVAILABLE_PIECES.length && i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int btnX = panelX + 4 + col * 24;
            int btnY = panelY + 4 + row * 24;

            if (mouseX >= btnX && mouseX < btnX + 20 && mouseY >= btnY && mouseY < btnY + 20) {
                // User clicked this piece - place it on the grid
                placePieceOnGrid(AVAILABLE_PIECES[i], selectionTargetX, selectionTargetY);
                closePieceSelection();
                return;
            }
        }
    }

    /**
     * Place a spell piece on the grid at the given coordinates.
     */
    private void placePieceOnGrid(String pieceId, int gridX, int gridY) {
        try {
            // Create a new piece instance from the registry
            vazkii.psi.api.spell.SpellPiece newPiece = vazkii.psi.common.spell.SpellPieceRegistry
                .create(pieceId, editingSpell);

            if (newPiece != null) {
                // Set piece position
                newPiece.x = gridX;
                newPiece.y = gridY;

                // Place on grid
                editingSpell.grid.gridData[gridX][gridY] = newPiece;
            }
        } catch (Exception e) {
            System.err.println("Failed to create spell piece: " + pieceId);
            e.printStackTrace();
        }
    }

    /**
     * Format piece registry name into readable text.
     * "psi:trick_break_block" -> "Trick Break Block"
     */
    private String formatPieceName(String registryName) {
        String[] parts = registryName.split(":");
        if (parts.length != 2) {
            return registryName;
        }

        String name = parts[1].replace("_", " ");
        String[] words = name.split(" ");
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

        return formatted.toString()
            .trim();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Handle piece selection overlay clicks
        if (pieceSelectionOpen) {
            handlePieceSelectionClick(mouseX, mouseY, button);
            return;
        }

        // Only handle grid clicks if cursor is over grid
        if (cursorX < 0 || cursorY < 0) {
            return;
        }

        // Right-click on grid
        if (button == 1) {
            vazkii.psi.api.spell.SpellPiece existingPiece = editingSpell.grid.gridData[cursorX][cursorY];

            // Right-click + Shift = Delete piece
            if (isShiftKeyDown() && existingPiece != null) {
                editingSpell.grid.gridData[cursorX][cursorY] = null;
                return;
            }

            // Right-click on empty cell = Open piece selection
            if (existingPiece == null) {
                openPieceSelection(cursorX, cursorY);
                return;
            }
        }
    }
}
