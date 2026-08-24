package vazkii.psi.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

    // Selection state (for parameter connections)
    private int selectedX = -1;
    private int selectedY = -1;

    // Piece selection state
    private boolean pieceSelectionOpen = false;
    private int selectionTargetX = -1;
    private int selectionTargetY = -1;

    // Parameter connection state
    private boolean paramSelectionOpen = false;
    private vazkii.psi.api.spell.SpellParam.Side clickedSide = vazkii.psi.api.spell.SpellParam.Side.OFF;

    // Search field for piece list
    private net.minecraft.client.gui.GuiTextField searchField;

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

        // Initialize search field (initially hidden)
        searchField = new net.minecraft.client.gui.GuiTextField(fontRendererObj, 0, 0, 100, 10);
        searchField.setMaxStringLength(50);
        searchField.setVisible(false);
        searchField.setEnableBackgroundDrawing(false);
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

        // Draw selection highlight (blue box) if a cell is selected
        if (selectedX >= 0 && selectedY >= 0) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            // Selection texture at (32, ySize) in programmer.png, size 16x16
            this.drawTexturedModalRect(
                gridLeft + selectedX * CELL_SIZE,
                gridTop + selectedY * CELL_SIZE,
                32,
                ySize,
                16,
                16);
        }

        // Draw hover highlight overlay from texture (only when piece list is NOT open)
        if (!pieceSelectionOpen && cursorX >= 0 && cursorY >= 0) {
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

        // Draw tooltips for hovered pieces (only when piece selection is NOT open)
        if (!pieceSelectionOpen && cursorX >= 0 && cursorY >= 0) {
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

        // Draw parameter selection overlay (on top of piece selection)
        if (paramSelectionOpen) {
            drawParamSelectionOverlay(mouseX, mouseY);
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
        // Calculate screen position
        int screenX = gridLeft + gridX * CELL_SIZE;
        int screenY = gridTop + gridY * CELL_SIZE;

        // Draw piece at grid position (icons are already 18x18 with transparent borders)
        PieceTextureAtlas.getInstance()
            .drawPiece(piece.registryKey.toString(), screenX, screenY);
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Don't pause in multiplayer
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // If piece selection is open, let search field handle typing
        if (pieceSelectionOpen && searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            return;
        }

        // ESC key (keyCode 1) closes the GUI or piece selection
        if (keyCode == 1) {
            if (pieceSelectionOpen) {
                closePieceSelection();
            } else {
                // Save spell before closing
                saveSpellToCAD();
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

        // Clear and focus search field
        searchField.setText("");
        searchField.setFocused(true);
    }

    /**
     * Close the piece selection overlay.
     */
    private void closePieceSelection() {
        pieceSelectionOpen = false;
        selectionTargetX = -1;
        selectionTargetY = -1;
        searchField.setVisible(false);
        searchField.setFocused(false);
    }

    /**
     * Detect which side of a cell was clicked.
     * Returns TOP, BOTTOM, LEFT, RIGHT, or OFF if center was clicked.
     */
    private vazkii.psi.api.spell.SpellParam.Side detectSideClick(int mouseX, int mouseY, int gridX, int gridY) {
        int cellX = gridLeft + gridX * CELL_SIZE;
        int cellY = gridTop + gridY * CELL_SIZE;

        // Get relative position within cell
        int relX = mouseX - cellX;
        int relY = mouseY - cellY;

        // Define edge detection zones (3px border on each side)
        int edgeSize = 4;

        if (relY < edgeSize) {
            return vazkii.psi.api.spell.SpellParam.Side.TOP;
        }
        if (relY >= CELL_SIZE - edgeSize) {
            return vazkii.psi.api.spell.SpellParam.Side.BOTTOM;
        }
        if (relX < edgeSize) {
            return vazkii.psi.api.spell.SpellParam.Side.LEFT;
        }
        if (relX >= CELL_SIZE - edgeSize) {
            return vazkii.psi.api.spell.SpellParam.Side.RIGHT;
        }

        return vazkii.psi.api.spell.SpellParam.Side.OFF;
    }

    /**
     * Open parameter selection panel for the clicked side.
     */
    private void openParamSelection(vazkii.psi.api.spell.SpellParam.Side side) {
        paramSelectionOpen = true;
        clickedSide = side;
    }

    /**
     * Close parameter selection panel.
     */
    private void closeParamSelection() {
        paramSelectionOpen = false;
        clickedSide = vazkii.psi.api.spell.SpellParam.Side.OFF;
    }

    /**
     * Handle clicks in parameter selection panel.
     */
    private void handleParamSelectionClick(int mouseX, int mouseY, int button) {
        if (button != 0) {
            closeParamSelection();
            return;
        }

        vazkii.psi.api.spell.SpellPiece selectedPiece = editingSpell.grid.gridData[selectedX][selectedY];
        if (selectedPiece == null || selectedPiece.params.isEmpty()) {
            closeParamSelection();
            return;
        }

        // Calculate panel position
        int panelX = gridLeft + (selectedX + 1) * CELL_SIZE;
        int panelY = gridTop + selectedY * CELL_SIZE;
        int panelWidth = 100;
        int paramHeight = 12;
        int panelHeight = paramHeight * selectedPiece.params.size() + 4;

        // Check if click is outside panel
        if (mouseX < panelX || mouseX >= panelX + panelWidth || mouseY < panelY || mouseY >= panelY + panelHeight) {
            closeParamSelection();
            return;
        }

        // Find which parameter was clicked
        int relY = mouseY - panelY - 2;
        int paramIndex = relY / paramHeight;

        if (paramIndex >= 0 && paramIndex < selectedPiece.params.size()) {
            // Get the param at this index
            java.util.List<vazkii.psi.api.spell.SpellParam<?>> paramList = new java.util.ArrayList<>(
                selectedPiece.params.values());
            vazkii.psi.api.spell.SpellParam<?> selectedParam = paramList.get(paramIndex);

            // Assign this param to the clicked side
            selectedPiece.paramSides.put(selectedParam, clickedSide);

            System.out.println(
                "[Psi] Assigned param '" + selectedParam.name
                    + "' to side "
                    + clickedSide
                    + " on piece at ("
                    + selectedX
                    + ","
                    + selectedY
                    + ")");

            // Sync to server
            syncSpellToServer();

            closeParamSelection();
        }
    }

    /**
     * Draw the piece selection overlay.
     */
    private void drawPieceSelectionOverlay(int mouseX, int mouseY) {
        // Panel dimensions - make it taller for list view
        int panelWidth = 100;
        int panelHeight = 125;

        // Position panel to the right of the selected cell (like 1.21.1)
        int panelX = gridLeft + (selectionTargetX + 1) * CELL_SIZE;
        int panelY = gridTop;

        // Draw panel background (no full-screen darkening, just the panel itself)
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC000000);

        // Draw search field at top
        searchField.xPosition = panelX + 10;
        searchField.yPosition = panelY + 5;
        searchField.width = panelWidth - 20;
        searchField.setVisible(true);
        searchField.drawTextBox();

        // Draw search icon (magnifying glass from texture atlas)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);
        this.drawTexturedModalRect(searchField.xPosition - 14, searchField.yPosition - 2, 0, ySize + 16, 12, 12);

        // Filter pieces based on search
        String searchText = searchField.getText()
            .toLowerCase();
        java.util.List<String> filteredPieces = new java.util.ArrayList<>();
        for (String piece : AVAILABLE_PIECES) {
            if (searchText.isEmpty() || piece.toLowerCase()
                .contains(searchText)
                || formatPieceName(piece).toLowerCase()
                    .contains(searchText)) {
                filteredPieces.add(piece);
            }
        }

        // Draw pieces in 5x5 grid (matching 1.21.1)
        int gridStartX = panelX + 5;
        int gridStartY = panelY + 20;
        int buttonSize = 18;
        int columns = 5;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        String hoveredPieceTooltip = null;

        for (int i = 0; i < filteredPieces.size(); i++) {
            String pieceId = filteredPieces.get(i);

            int col = i % columns;
            int row = i / columns;
            int btnX = gridStartX + col * buttonSize;
            int btnY = gridStartY + row * buttonSize;

            // Check if mouse is over this button
            boolean hovered = mouseX >= btnX && mouseX < btnX + buttonSize
                && mouseY >= btnY
                && mouseY < btnY + buttonSize;

            // Button background
            if (hovered) {
                drawRect(btnX, btnY, btnX + buttonSize, btnY + buttonSize, 0x885555FF);
            }

            // Draw piece icon using texture atlas (icons are 18x18 with transparent borders)
            PieceTextureAtlas.getInstance()
                .drawPiece(pieceId, btnX, btnY);

            // Store tooltip for rendering last
            if (hovered) {
                hoveredPieceTooltip = formatPieceName(pieceId);
            }
        }

        // Draw "No results" message if search filtered everything
        if (filteredPieces.isEmpty()) {
            String noResults = "No pieces found";
            int textX = panelX + (panelWidth - fontRendererObj.getStringWidth(noResults)) / 2;
            fontRendererObj.drawString(noResults, textX, gridStartY + 20, 0x888888);
        }

        // Draw tooltip LAST so it renders on top of everything
        if (hoveredPieceTooltip != null) {
            java.util.List<String> tooltipList = new java.util.ArrayList<>();
            tooltipList.add(hoveredPieceTooltip);
            this.drawHoveringText(tooltipList, mouseX, mouseY, fontRendererObj);
        }
    }

    /**
     * Handle mouse clicks in the piece selection overlay.
     */
    private void handlePieceSelectionClick(int mouseX, int mouseY, int button) {
        // Let search field handle clicks
        searchField.mouseClicked(mouseX, mouseY, button);

        // Only handle left-clicks for piece selection
        if (button != 0) {
            closePieceSelection();
            return;
        }

        // Calculate panel position (same as in drawPieceSelectionOverlay)
        int panelWidth = 100;
        int panelHeight = 125;
        int panelX = gridLeft + (selectionTargetX + 1) * CELL_SIZE;
        int panelY = gridTop;

        // Check if click is inside panel
        boolean insidePanel = mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= panelY
            && mouseY < panelY + panelHeight;

        if (!insidePanel) {
            closePieceSelection();
            return;
        }

        // Filter pieces based on search
        String searchText = searchField.getText()
            .toLowerCase();
        java.util.List<String> filteredPieces = new java.util.ArrayList<>();
        for (String piece : AVAILABLE_PIECES) {
            if (searchText.isEmpty() || piece.toLowerCase()
                .contains(searchText)
                || formatPieceName(piece).toLowerCase()
                    .contains(searchText)) {
                filteredPieces.add(piece);
            }
        }

        // Check which piece was clicked in the 5x5 grid
        int gridStartX = panelX + 5;
        int gridStartY = panelY + 20;
        int buttonSize = 18;
        int columns = 5;

        for (int i = 0; i < filteredPieces.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int btnX = gridStartX + col * buttonSize;
            int btnY = gridStartY + row * buttonSize;

            if (mouseX >= btnX && mouseX < btnX + buttonSize && mouseY >= btnY && mouseY < btnY + buttonSize) {
                // User clicked this piece - place it on the grid
                placePieceOnGrid(filteredPieces.get(i), selectionTargetX, selectionTargetY);
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

                // Debug: Check registryKey
                System.out.println("[Psi] Placed piece: " + pieceId + " -> registryKey: " + newPiece.registryKey);

                // Place on grid
                editingSpell.grid.gridData[gridX][gridY] = newPiece;

                // Sync to server immediately
                syncSpellToServer();
            } else {
                System.err.println("[Psi] Failed to create piece: " + pieceId + " (create returned null)");
            }
        } catch (Exception e) {
            System.err.println("Failed to create spell piece: " + pieceId);
            e.printStackTrace();
        }
    }

    /**
     * Synchronize the current spell to the server.
     * Sends a packet with the spell's NBT data so changes persist.
     * 
     * /**
     * Draw the parameter selection overlay.
     */
    private void drawParamSelectionOverlay(int mouseX, int mouseY) {
        vazkii.psi.api.spell.SpellPiece selectedPiece = editingSpell.grid.gridData[selectedX][selectedY];
        if (selectedPiece == null || selectedPiece.params.isEmpty()) {
            closeParamSelection();
            return;
        }

        // Calculate panel position (to the right of selected piece)
        int panelX = gridLeft + (selectedX + 1) * CELL_SIZE;
        int panelY = gridTop + selectedY * CELL_SIZE;
        int panelWidth = 100;
        int paramHeight = 12;
        int panelHeight = paramHeight * selectedPiece.params.size() + 4;

        // Draw panel background
        drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC000000);
        drawRect(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF888888); // Top border

        // Draw side indicator
        String sideText = "Side: " + clickedSide.name();
        fontRendererObj.drawString(sideText, panelX + 4, panelY + 2, 0xFFFFFF);

        // Draw each parameter as a clickable option
        int y = panelY + 14;
        int index = 0;
        for (vazkii.psi.api.spell.SpellParam<?> param : selectedPiece.params.values()) {
            boolean hovered = mouseX >= panelX && mouseX < panelX + panelWidth
                && mouseY >= y
                && mouseY < y + paramHeight;

            // Highlight if hovered
            if (hovered) {
                drawRect(panelX + 2, y, panelX + panelWidth - 2, y + paramHeight, 0x44FFFFFF);
            }

            // Check if this param is already assigned to the clicked side
            boolean isAssigned = selectedPiece.paramSides.get(param) == clickedSide;
            int color = isAssigned ? 0x55FF55 : 0xFFFFFF;

            // Draw parameter name
            String paramName = param.name;
            fontRendererObj.drawString(paramName, panelX + 4, y + 2, color);

            y += paramHeight;
            index++;
        }
    }

    /**
     * Synchronize the current spell to the server.
     * Sends a packet with the spell's NBT data so changes persist.
     */
    private void syncSpellToServer() {
        if (editingSpell != null && cadStack != null) {
            NBTTagCompound spellNBT = new NBTTagCompound();
            editingSpell.writeToNBT(spellNBT);

            // Send packet to server
            vazkii.psi.common.network.PacketHandler.INSTANCE
                .sendToServer(new vazkii.psi.common.network.PacketSpellUpdate(editingSpell));

            System.out.println("[Psi] Synced spell '" + editingSpell.name + "' to server");
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

        // Handle parameter selection overlay clicks
        if (paramSelectionOpen) {
            handleParamSelectionClick(mouseX, mouseY, button);
            return;
        }

        // Only handle grid clicks if cursor is over grid
        if (cursorX < 0 || cursorY < 0) {
            return;
        }

        // Left-click = Select piece OR assign parameter side
        if (button == 0) {
            // Check if clicking on an already selected piece
            if (selectedX == cursorX && selectedY == cursorY) {
                vazkii.psi.api.spell.SpellPiece piece = editingSpell.grid.gridData[cursorX][cursorY];
                if (piece != null && !piece.params.isEmpty()) {
                    // Detect which side was clicked
                    vazkii.psi.api.spell.SpellParam.Side side = detectSideClick(mouseX, mouseY, cursorX, cursorY);
                    if (side.isEnabled()) {
                        openParamSelection(side);
                        return;
                    }
                }
            }
            // Otherwise, just select the piece
            selectedX = cursorX;
            selectedY = cursorY;
            return;
        }

        // Right-click on grid
        if (button == 1) {
            vazkii.psi.api.spell.SpellPiece existingPiece = editingSpell.grid.gridData[cursorX][cursorY];

            // Right-click + Shift = Delete piece
            if (isShiftKeyDown() && existingPiece != null) {
                editingSpell.grid.gridData[cursorX][cursorY] = null;
                syncSpellToServer(); // Sync deletion to server
                return;
            }

            // Right-click on any cell (empty or occupied) = Open piece selection to place/replace
            openPieceSelection(cursorX, cursorY);
            return;
        }
    }

    /**
     * Save the current spell to the CAD item.
     */
    private void saveSpellToCAD() {
        if (editingSpell != null && cadStack != null) {
            ItemCAD.setSpell(cadStack, editingSpell);
        }
    }
}
