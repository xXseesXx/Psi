package vazkii.psi.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.spell.constant.PieceConstantNumber;

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

    // Search field for piece list
    private net.minecraft.client.gui.GuiTextField searchField;
    private net.minecraft.client.gui.GuiTextField spellNameField;
    private SpellCompilationException compilationError;

    private int panelPage;
    private int panelCursor;
    private static final int PIECES_PER_PAGE = 25;

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

        spellNameField = new net.minecraft.client.gui.GuiTextField(fontRendererObj, guiLeft + xSize - 130,
            guiTop + ySize - 14, 120, 10);
        spellNameField.setMaxStringLength(20);
        spellNameField.setEnableBackgroundDrawing(false);
        spellNameField.setText(editingSpell.name == null ? "" : editingSpell.name);
        recompileSpell();

        // GuiProgrammer's selected coordinates are static and therefore begin
        // at (0, 0).  Mirroring that here makes keyboard navigation usable
        // before the player has clicked a piece.
        if (selectedX < 0 || selectedY < 0) {
            selectedX = 0;
            selectedY = 0;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();
        spellNameField.updateCursorCounter();
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
        // The picker, not the side configuration widget, owns mouse input while
        // open.  This is what lets the original retain grid hover while config
        // buttons are visible.
        if (pieceSelectionOpen || cursorX > 8
            || cursorY > 8
            || cursorX < 0
            || cursorY < 0
            || mouseX < gridLeft
            || mouseY < gridTop) {
            cursorX = -1;
            cursorY = -1;
        }

        // Draw spell pieces on grid FIRST
        drawSpellPieces();

        // Draw parameter connection lines
        drawConnectionLines();
        drawCompilationError();

        // Draw selection highlight (blue box) if a cell is selected - AFTER pieces
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

        // Draw hover after selection.  The original uses the left half of the
        // hover sprite when both markers occupy the same cell, so both remain
        // legible instead of one completely obscuring the other.
        if (!pieceSelectionOpen && cursorX >= 0 && cursorY >= 0) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            // Hover overlay texture at (16, ySize) in programmer.png, size 16x16
            int hoverWidth = cursorX == selectedX && cursorY == selectedY ? 8 : 16;
            this.drawTexturedModalRect(gridLeft + cursorX * CELL_SIZE, gridTop + cursorY * CELL_SIZE, 16, ySize,
                hoverWidth, 16);
        }

        drawSelectedPieceLabel();

        // Keep config rendering ahead of tooltips.  Tooltip rendering changes
        // GL state in 1.7.10; drawing the panel afterwards was the source of
        // its washed-out appearance while hovering a grid piece.
        drawSideConfigPanel(mouseX, mouseY);
        drawSpellNameField();

        // Tooltip rendering in 1.7.10 leaves a dark blend state. Render the
        // right-side widgets first, as with the configuration panel.
        drawClipboardButtons(mouseX, mouseY);

        // Draw tooltips for hovered pieces (only when piece selection is NOT open)
        if (!pieceSelectionOpen && cursorX >= 0 && cursorY >= 0) {
            vazkii.psi.api.spell.SpellPiece hoveredPiece = editingSpell.grid.gridData[cursorX][cursorY];
            if (hoveredPiece != null) {
                java.util.List<String> tooltip = buildPieceTooltip(hoveredPiece);
                this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw piece selection overlay LAST (on top of everything)
        if (pieceSelectionOpen) {
            drawPieceSelectionOverlay(mouseX, mouseY);
        }

    }

    private void drawCompilationError() {
        if (compilationError == null || compilationError.x < 0 || compilationError.y < 0) return;
        fontRendererObj.drawStringWithShadow("!!", gridLeft + compilationError.x * CELL_SIZE + 12,
            gridTop + compilationError.y * CELL_SIZE + 8, 0xFF0000);
    }

    private void drawSpellNameField() {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        fontRendererObj.drawString(net.minecraft.client.resources.I18n.format("psimisc.name"), guiLeft + padLeft,
            spellNameField.yPosition + 1, 0xFFFFFF);
        spellNameField.drawTextBox();
    }

    private void recompileSpell() {
        try {
            new SpellCompiler().compile(editingSpell);
            compilationError = null;
        } catch (SpellCompilationException error) {
            compilationError = error;
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

        if (piece instanceof PieceConstantNumber) {
            String value = ((PieceConstantNumber) piece).getDisplayValue();
            if (value.length() > 5) {
                value = value.substring(0, 5);
            }
            int x = screenX + (CELL_SIZE - fontRendererObj.getStringWidth(value)) / 2;
            fontRendererObj.drawStringWithShadow(value, x, screenY + 5, 0xFFFFFF);
        }
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
            java.util.List<String> pieces = getFilteredPieces();
            if (keyCode == Keyboard.KEY_ESCAPE) {
                closePieceSelection();
                return;
            }
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                int index = panelPage * PIECES_PER_PAGE + panelCursor;
                if (index >= 0 && index < pieces.size()) {
                    placePieceOnGrid(pieces.get(index), selectionTargetX, selectionTargetY);
                    closePieceSelection();
                }
                return;
            }
            if (keyCode == Keyboard.KEY_TAB && !pieces.isEmpty()) {
                int visible = Math.min(PIECES_PER_PAGE, pieces.size() - panelPage * PIECES_PER_PAGE);
                panelCursor += isShiftKeyDown() ? -1 : 1;
                if (panelCursor < 0) panelCursor = visible - 1;
                if (panelCursor >= visible) panelCursor = 0;
                return;
            }
            if (keyCode == Keyboard.KEY_PRIOR && panelPage > 0) {
                panelPage--;
                panelCursor = 0;
                return;
            }
            int pageCount = Math.max(1, (pieces.size() + PIECES_PER_PAGE - 1) / PIECES_PER_PAGE);
            if (keyCode == Keyboard.KEY_NEXT && panelPage + 1 < pageCount) {
                panelPage++;
                panelCursor = 0;
                return;
            }
            searchField.textboxKeyTyped(typedChar, keyCode);
            panelPage = 0;
            panelCursor = 0;
            return;
        }

        if (spellNameField.isFocused()) {
            if (keyCode == Keyboard.KEY_TAB) {
                spellNameField.setFocused(false);
                return;
            }
            if (spellNameField.textboxKeyTyped(typedChar, keyCode)) {
                editingSpell.name = spellNameField.getText();
                syncSpellToServer();
            }
            return;
        }

        vazkii.psi.api.spell.SpellPiece selectedPiece = getSelectedPiece();
        if (selectedPiece != null && !selectedPiece.params.isEmpty() && keyCode >= Keyboard.KEY_1
            && keyCode <= Keyboard.KEY_4) {
            int requested = keyCode - Keyboard.KEY_1;
            if (requested < selectedPiece.params.size()) {
                return;
            }
        }
        if (selectedPiece != null && selectedPiece.interceptKeystrokes()) {
            if (selectedPiece.onKeyPressed(keyCode, false)) {
                selectedPiece.onKeyPressed(keyCode, true);
                syncSpellToServer();
                return;
            }
            if (selectedPiece.onCharTyped(typedChar, keyCode, false)) {
                selectedPiece.onCharTyped(typedChar, keyCode, true);
                syncSpellToServer();
                return;
            }
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
            return;
        }

        if (!pieceSelectionOpen) {
            int param = getHeldParameterIndex();
            if (keyCode == Keyboard.KEY_UP) {
                if (selectedPiece != null && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.TOP)) return;
                moveSelection(0, -1);
            } else if (keyCode == Keyboard.KEY_DOWN) {
                if (selectedPiece != null && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.BOTTOM)) return;
                moveSelection(0, 1);
            } else if (keyCode == Keyboard.KEY_LEFT) {
                if (selectedPiece != null && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.LEFT)) return;
                moveSelection(-1, 0);
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                if (selectedPiece != null && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.RIGHT)) return;
                moveSelection(1, 0);
            }
        }
    }

    private vazkii.psi.api.spell.SpellPiece getSelectedPiece() {
        if (selectedX < 0 || selectedY < 0 || editingSpell == null || editingSpell.grid == null) return null;
        return editingSpell.grid.gridData[selectedX][selectedY];
    }

    private void moveSelection(int offsetX, int offsetY) {
        int nextX = selectedX + offsetX;
        int nextY = selectedY + offsetY;
        if (nextX >= 0 && nextX < GRID_SIZE && nextY >= 0 && nextY < GRID_SIZE) {
            selectedX = nextX;
            selectedY = nextY;
        }
    }

    private int getHeldParameterIndex() {
        for (int i = 0; i < 4; i++) {
            if (Keyboard.isKeyDown(Keyboard.KEY_1 + i)) return i;
        }
        return -1;
    }

    private boolean isAltHeld() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    private boolean onSideButtonKeybind(vazkii.psi.api.spell.SpellPiece piece, int paramIndex,
        vazkii.psi.api.spell.SpellParam.Side side) {
        if (paramIndex < 0 || paramIndex >= piece.params.size()) return false;
        vazkii.psi.api.spell.SpellParam<?> param = new java.util.ArrayList<vazkii.psi.api.spell.SpellParam<?>>(piece.params.values())
            .get(paramIndex);
        if (side == vazkii.psi.api.spell.SpellParam.Side.OFF && !param.canDisable) return false;
        piece.paramSides.put(param, side);
        syncSpellToServer();
        return true;
    }

    private void drawSelectedPieceLabel() {
        vazkii.psi.api.spell.SpellPiece piece = getSelectedPiece();
        if (piece == null || pieceSelectionOpen) return;
        String name = net.minecraft.client.resources.I18n.format(piece.getUnlocalizedName());
        fontRendererObj.drawStringWithShadow(name, guiLeft + (xSize - fontRendererObj.getStringWidth(name)) / 2, guiTop - 22,
            0xFFFFFF);
    }

    private java.util.List<String> buildPieceTooltip(vazkii.psi.api.spell.SpellPiece piece) {
        java.util.List<String> tooltip = new java.util.ArrayList<String>();
        piece.getTooltip(tooltip);
        if (isShiftKeyDown()) {
            tooltip.add("\u00a77Parameters:");
            for (vazkii.psi.api.spell.SpellParam<?> param : piece.params.values()) {
                tooltip.add("\u00a77- " + net.minecraft.client.resources.I18n.format(param.name) + ": "
                    + param.getRequiredTypeString());
            }
        }
        if (isCtrlKeyDown()) tooltip.add("\u00a77" + piece.getEvaluationTypeString());
        return tooltip;
    }

    /**
     * Open the piece selection overlay at the given grid coordinates.
     */
    private void openPieceSelection(int gridX, int gridY) {
        pieceSelectionOpen = true;
        selectionTargetX = gridX;
        selectionTargetY = gridY;
        selectedX = gridX;
        selectedY = gridY;

        // Clear and focus search field
        searchField.setText("");
        searchField.setFocused(true);
        panelPage = 0;
        panelCursor = 0;
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
        panelPage = 0;
        panelCursor = 0;
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
        searchField.xPosition = panelX + 18;
        searchField.yPosition = panelY + 5;
        searchField.width = 70;
        searchField.setVisible(true);
        searchField.drawTextBox();

        // Draw search icon (magnifying glass from texture atlas)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);
        this.drawTexturedModalRect(searchField.xPosition - 14, searchField.yPosition - 2, 0, ySize + 16, 12, 12);

        java.util.List<String> filteredPieces = getFilteredPieces();

        // Draw pieces in 5x5 grid (matching 1.21.1)
        int gridStartX = panelX + 5;
        int gridStartY = panelY + 20;
        int buttonSize = 18;
        int columns = 5;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        java.util.List<String> hoveredPieceTooltip = null;

        int first = panelPage * PIECES_PER_PAGE;
        int last = Math.min(first + PIECES_PER_PAGE, filteredPieces.size());
        for (int i = first; i < last; i++) {
            String pieceId = filteredPieces.get(i);

            int visibleIndex = i - first;
            int col = visibleIndex % columns;
            int row = visibleIndex / columns;
            int btnX = gridStartX + col * buttonSize;
            int btnY = gridStartY + row * buttonSize;

            // Check if mouse is over this button
            boolean hovered = mouseX >= btnX && mouseX < btnX + buttonSize
                && mouseY >= btnY
                && mouseY < btnY + buttonSize;

            // Keyboard selection is a separate blue marker and belongs behind
            // the piece. Mouse hover is drawn over it with the grid sprite.
            if (visibleIndex == panelCursor) {
                drawRect(btnX - 1, btnY - 1, btnX + 17, btnY + 17, 0x559999FF);
            }

            // Draw piece icon using texture atlas (icons are 18x18 with transparent borders)
            PieceTextureAtlas.getInstance()
                .drawPiece(pieceId, btnX, btnY);

            // Mouse hover uses the exact same sprite as the spell grid, over
            // the icon. The blue keyboard marker above remains behind it.
            if (hovered) {
                this.mc.getTextureManager().bindTexture(TEXTURE);
                this.drawTexturedModalRect(btnX, btnY, 16, ySize, 16, 16);
            }

            // Store tooltip for rendering last
            if (hovered) {
                vazkii.psi.api.spell.SpellPiece preview = vazkii.psi.common.spell.SpellPieceRegistry.create(pieceId,
                    editingSpell);
                if (preview != null) {
                    java.util.List<String> tooltip = buildPieceTooltip(preview);
                    hoveredPieceTooltip = tooltip;
                }
            }
        }

        // Draw "No results" message if search filtered everything
        if (filteredPieces.isEmpty()) {
            String noResults = "No pieces found";
            int textX = panelX + (panelWidth - fontRendererObj.getStringWidth(noResults)) / 2;
            fontRendererObj.drawString(noResults, textX, gridStartY + 20, 0x888888);
        }

        int pageCount = Math.max(1, (filteredPieces.size() + PIECES_PER_PAGE - 1) / PIECES_PER_PAGE);
        String pageText = (panelPage + 1) + "/" + pageCount;
        fontRendererObj.drawString(pageText, panelX + (panelWidth - fontRendererObj.getStringWidth(pageText)) / 2,
            panelY + panelHeight - 12, 0xFFFFFF);

        // Draw tooltip LAST so it renders on top of everything
        if (hoveredPieceTooltip != null) {
            this.drawHoveringText(hoveredPieceTooltip, mouseX, mouseY, fontRendererObj);
        }
    }

    /**
     * Handle mouse clicks in the piece selection overlay.
     */
    private void handlePieceSelectionClick(int mouseX, int mouseY, int button) {
        // Let search field handle clicks
        searchField.mouseClicked(mouseX, mouseY, button);

        // Calculate panel position (same as in drawPieceSelectionOverlay)
        int panelWidth = 100;
        int panelHeight = 125;
        int panelX = gridLeft + (selectionTargetX + 1) * CELL_SIZE;
        int panelY = gridTop;

        // Check if click is inside panel
        boolean insidePanel = mouseX >= panelX && mouseX < panelX + panelWidth
            && mouseY >= panelY
            && mouseY < panelY + panelHeight;

        // Right-click is a cancel action only outside the picker. This allows
        // normal right-clicks in the search/piece area without closing it.
        if (button != 0) {
            if (!insidePanel) closePieceSelection();
            return;
        }

        if (!insidePanel) {
            closePieceSelection();
            return;
        }

        java.util.List<String> filteredPieces = getFilteredPieces();

        // Check which piece was clicked in the 5x5 grid
        int gridStartX = panelX + 5;
        int gridStartY = panelY + 20;
        int buttonSize = 18;
        int columns = 5;

        int first = panelPage * PIECES_PER_PAGE;
        int last = Math.min(first + PIECES_PER_PAGE, filteredPieces.size());
        for (int i = first; i < last; i++) {
            int visibleIndex = i - first;
            int col = visibleIndex % columns;
            int row = visibleIndex / columns;
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

                if (newPiece instanceof PieceConstantNumber && isNumber(searchField.getText())) {
                    ((PieceConstantNumber) newPiece).constant = Double.parseDouble(searchField.getText());
                }

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

    private java.util.List<String> getFilteredPieces() {
        String searchText = searchField.getText().toLowerCase(java.util.Locale.ROOT).trim();
        java.util.List<String> pieces = new java.util.ArrayList<String>();
        for (String piece : vazkii.psi.common.spell.SpellPieceRegistry.getRegisteredIds()) {
            String name = formatPieceName(piece).toLowerCase(java.util.Locale.ROOT);
            if (searchText.isEmpty() || piece.toLowerCase(java.util.Locale.ROOT).contains(searchText) || name.contains(searchText)) {
                pieces.add(piece);
            }
        }
        return pieces;
    }

    private boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return value.length() <= 5;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * 
     * /**
     * Draw the side configuration panel on the left when a piece with parameters is selected.
     */

    /**
     * Draw parameter arrows on spell pieces to show connections. Matches 1.21.1 implementation.
     */
    private void drawConnectionLines() {
        if (editingSpell == null || editingSpell.grid == null) {
            return;
        }

        // Bind the programmer texture for arrow sprites
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Start batched rendering for all arrows
        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.instance;
        tess.startDrawingQuads();

        // Iterate through all pieces and draw their parameter arrows
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                vazkii.psi.api.spell.SpellPiece piece = editingSpell.grid.gridData[x][y];
                if (piece == null || piece.paramSides.isEmpty()) {
                    continue;
                }

                // Draw each parameter arrow on this piece
                for (java.util.Map.Entry<vazkii.psi.api.spell.SpellParam<?>, vazkii.psi.api.spell.SpellParam.Side> entry : piece.paramSides
                    .entrySet()) {
                    vazkii.psi.api.spell.SpellParam<?> param = entry.getKey();
                    vazkii.psi.api.spell.SpellParam.Side side = entry.getValue();

                    if (!side.isEnabled()) {
                        continue;
                    }

                    // Calculate arrow index (which arrow on this side) and count (total arrows on this side)
                    int index = getParamArrowIndex(piece, param);
                    int count = getParamArrowCount(piece, side);

                    // If there's a neighbor piece, adjust positioning to avoid overlap
                    vazkii.psi.api.spell.SpellPiece neighbor = editingSpell.grid.getPieceAtSideSafely(x, y, side);
                    if (neighbor != null) {
                        int nbCount = getParamArrowCount(neighbor, side.getOpposite());
                        if (side.asInt() > side.getOpposite()
                            .asInt()) {
                            index += nbCount;
                        }
                        count += nbCount;
                    }

                    // Calculate position percentage along the edge
                    float percent = 0.5F;
                    if (count > 1) {
                        percent = (float) index / (count - 1);
                    }

                    // Add arrow vertices to batch
                    addParamArrowToBatch(tess, x, y, side, param.color, percent);
                }
            }
        }

        // Draw all arrows at once
        tess.draw();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Get the index of this parameter's arrow among all arrows on the same side.
     */
    private int getParamArrowIndex(vazkii.psi.api.spell.SpellPiece piece, vazkii.psi.api.spell.SpellParam<?> target) {
        int index = 0;
        for (vazkii.psi.api.spell.SpellParam<?> param : piece.paramSides.keySet()) {
            if (param == target) {
                return index;
            }
            if (piece.paramSides.get(param) == piece.paramSides.get(target)) {
                index++;
            }
        }
        return index;
    }

    /**
     * Count how many parameter arrows are on this side of the piece.
     */
    private int getParamArrowCount(vazkii.psi.api.spell.SpellPiece piece, vazkii.psi.api.spell.SpellParam.Side side) {
        int count = 0;
        for (vazkii.psi.api.spell.SpellParam<?> param : piece.paramSides.keySet()) {
            if (piece.paramSides.get(param) == side) {
                count++;
            }
        }
        return count;
    }

    /**
     * Add a single parameter arrow to the Tessellator batch.
     */
    private void addParamArrowToBatch(net.minecraft.client.renderer.Tessellator tess, int pieceX, int pieceY,
        vazkii.psi.api.spell.SpellParam.Side side, int color, float percent) {
        // Calculate position on the edge of the piece
        // side.minx/miny/maxx/maxy define the bounding box for arrow placement
        float minX = 4 + side.minx * percent + side.maxx * (1 - percent);
        float minY = 4 + side.miny * percent + side.maxy * (1 - percent);
        float maxX = minX + 8;
        float maxY = minY + 8;

        // Convert to screen coordinates
        int screenX = gridLeft + pieceX * CELL_SIZE;
        int screenY = gridTop + pieceY * CELL_SIZE;

        // Extract color components
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Texture coordinates for the arrow sprite (8x8 pixels)
        float minU = side.u / 256.0F;
        float minV = side.v / 256.0F;
        float maxU = (side.u + 8) / 256.0F;
        float maxV = (side.v + 8) / 256.0F;

        // Add vertices with color
        tess.setColorRGBA(r, g, b, 255);
        tess.addVertexWithUV(screenX + minX, screenY + maxY, zLevel, minU, maxV);
        tess.addVertexWithUV(screenX + maxX, screenY + maxY, zLevel, maxU, maxV);
        tess.addVertexWithUV(screenX + maxX, screenY + minY, zLevel, maxU, minV);
        tess.addVertexWithUV(screenX + minX, screenY + minY, zLevel, minU, minV);
    }

    /**
     * Check if the side configuration panel is currently visible.
     * Matches 1.21.1's panelWidget.panelEnabled check.
     */
    private boolean isSideConfigPanelOpen() {
        if (selectedX < 0 || selectedY < 0 || editingSpell == null || editingSpell.grid == null) {
            return false;
        }
        vazkii.psi.api.spell.SpellPiece piece = editingSpell.grid.gridData[selectedX][selectedY];
        return piece != null && !piece.params.isEmpty();
    }

    private void drawSideConfigPanel(int mouseX, int mouseY) {
        // Check if we have a selected piece with parameters
        if (selectedX < 0 || selectedY < 0) {
            return;
        }

        vazkii.psi.api.spell.SpellPiece piece = editingSpell.grid.gridData[selectedX][selectedY];
        if (piece == null || piece.params.isEmpty()) {
            return;
        }

        // Draw panel background (texture from programmer.png at xSize, 30)
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);
        this.drawTexturedModalRect(guiLeft - 81, guiTop + 55, xSize, 30, 81, 115);

        // Draw "Config" label (translated)
        String configText = net.minecraft.client.resources.I18n.format("psimisc.config");
        fontRendererObj
            .drawString(configText, guiLeft - fontRendererObj.getStringWidth(configText) - 2, guiTop + 45, 0xFFFFFF);

        // Draw each parameter with its side buttons
        int paramIndex = 0;
        for (String paramName : piece.params.keySet()) {
            vazkii.psi.api.spell.SpellParam<?> param = piece.params.get(paramName);

            int panelX = guiLeft - 75;
            int panelY = guiTop + 70 + paramIndex * 26;

            // Draw parameter icon background (24x24 slot at xSize, 145)
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            this.drawTexturedModalRect(panelX + 50, panelY - 8, xSize, 145, 24, 24);

            // Pressing 1-4 selects a parameter until another is selected.
            // Underline the active one, like the modern programmer.
            String translatedName = net.minecraft.client.resources.I18n.format(param.name);
            if (paramIndex == getHeldParameterIndex()) {
                translatedName = "\u00a7n" + translatedName;
            }
            fontRendererObj.drawString(translatedName, panelX, panelY, 0xFFFFFF);

            // Draw side buttons (OFF, TOP, BOTTOM, LEFT, RIGHT)
            drawSideButtons(panelX, panelY, paramIndex, param, piece, mouseX, mouseY);

            paramIndex++;
        }
    }

    /**
     * Draw the 5 side configuration buttons for a parameter.
     */
    private void drawSideButtons(int x, int y, int paramIndex, vazkii.psi.api.spell.SpellParam<?> param,
        vazkii.psi.api.spell.SpellPiece piece, int mouseX, int mouseY) {
        // Button positions around the parameter icon
        // Center button (OFF) is at (x+58, y)
        // Others are offset by 8 pixels in their direction

        vazkii.psi.api.spell.SpellParam.Side currentSide = piece.paramSides.get(param);
        if (currentSide == null) {
            currentSide = vazkii.psi.api.spell.SpellParam.Side.OFF;
        }

        for (vazkii.psi.api.spell.SpellParam.Side side : vazkii.psi.api.spell.SpellParam.Side.values()) {
            // Skip OFF if param can't be disabled
            if (side == vazkii.psi.api.spell.SpellParam.Side.OFF && !param.canDisable) {
                continue;
            }

            // Calculate button position
            int btnX = x + 58 + side.offx * 8;
            int btnY = y + side.offy * 8;

            // Set color based on whether this side is currently selected
            if (currentSide == side) {
                // Highlight with parameter color
                int r = (param.color >> 16) & 0xFF;
                int g = (param.color >> 8) & 0xFF;
                int b = param.color & 0xFF;
                GL11.glColor4f(r / 255F, g / 255F, b / 255F, 1.0F);
            } else {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            // Draw button icon (8x8 from texture at side.u, side.v)
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            this.drawTexturedModalRect(btnX, btnY, side.u, side.v, 8, 8);

        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Synchronize the current spell to the server.
     * Sends a packet with the spell's NBT data so changes persist.
     */
    private void syncSpellToServer() {
        if (editingSpell != null && cadStack != null) {
            recompileSpell();
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

    /** Draws the modern programmer's export/import buttons. */
    private void drawClipboardButtons(int mouseX, int mouseY) {
        int x = guiLeft + xSize + 2;
        int helpY = guiTop + ySize - 48;
        int exportY = guiTop + ySize - 32;
        int importY = guiTop + ySize - 16;
        boolean overHelp = isOver(mouseX, mouseY, x, helpY, 12, 12);
        boolean overExport = isOver(mouseX, mouseY, x, exportY, 12, 12);
        boolean overImport = isOver(mouseX, mouseY, x, importY, 12, 12);

        // Tooltips in 1.7.10 alter GL colour state. Reset it explicitly because
        // these controls must never inherit a dimmed colour from grid/picker UI.
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(x, helpY, xSize + (overHelp ? 12 : 0), ySize + 9, 12, 12);
        this.drawTexturedModalRect(x, exportY, overExport ? 186 : 174, 169, 12, 12);
        this.drawTexturedModalRect(x, importY, overImport ? 186 : 174, 181, 12, 12);

        if (overHelp && !isAltHeld()) {
            java.util.List<String> tooltip = new java.util.ArrayList<String>();
            tooltip.add("\u00a7aProgrammer Help");
            if (isShiftKeyDown()) {
                for (int i = 0; i <= 22; i++) {
                    String key = "psi.programmer_reference" + i;
                    String line = net.minecraft.client.resources.I18n.format(key, "Ctrl");
                    if (!line.equals(key)) tooltip.add(line);
                }
            } else {
                tooltip.add("\u00a77Hold SHIFT for more info");
            }
            this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
        } else if (overExport || overImport) {
            java.util.List<String> tooltip = new java.util.ArrayList<String>();
            tooltip.add(overExport ? "\u00a7cExport to Clipboard" : "\u00a79Import from Clipboard");
            tooltip.add("\u00a77(Must be holding SHIFT)");
            this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
        }
    }

    private boolean handleClipboardButtonClick(int mouseX, int mouseY) {
        int x = guiLeft + xSize + 2;
        int helpY = guiTop + ySize - 48;
        int exportY = guiTop + ySize - 32;
        int importY = guiTop + ySize - 16;
        if (isOver(mouseX, mouseY, x, helpY, 12, 12)) {
            return true; // The original help button is intentionally informational.
        }
        if (!isOver(mouseX, mouseY, x, exportY, 12, 12) && !isOver(mouseX, mouseY, x, importY, 12, 12)) {
            return false;
        }
        if (!isShiftKeyDown()) {
            return true;
        }
        if (isOver(mouseX, mouseY, x, exportY, 12, 12)) {
            NBTTagCompound spellTag = new NBTTagCompound();
            editingSpell.writeToNBT(spellTag);
            GuiScreen.setClipboardString(spellTag.toString());
            return true;
        }
        try {
            NBTBase parsed = JsonToNBT.func_150315_a(GuiScreen.getClipboardString());
            if (parsed instanceof NBTTagCompound) {
                editingSpell = Spell.readFromNBT((NBTTagCompound) parsed);
                spellNameField.setText(editingSpell.name == null ? "" : editingSpell.name);
                syncSpellToServer();
            }
        } catch (Exception ignored) {
            // Keep the current spell intact when the clipboard is not spell NBT.
        }
        return true;
    }

    private boolean isOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (!pieceSelectionOpen) {
            spellNameField.mouseClicked(mouseX, mouseY, button);
            if (isOver(mouseX, mouseY, spellNameField.xPosition, spellNameField.yPosition, spellNameField.width,
                spellNameField.height)) {
                spellNameField.setFocused(true);
                return;
            }
        }
        spellNameField.setFocused(false);

        // The right-side controls remain active above the piece picker too.
        if (button == 0 && handleClipboardButtonClick(mouseX, mouseY)) {
            return;
        }

        // Handle piece selection overlay clicks
        if (pieceSelectionOpen) {
            handlePieceSelectionClick(mouseX, mouseY, button);
            return;
        }

        // Handle side config button clicks
        if (handleSideConfigClick(mouseX, mouseY, button)) {
            return;
        }

        // Calculate cursor position for click handling (independent of hover rendering)
        // This allows clicking pieces even when side config panel is open
        int clickCursorX = (mouseX - gridLeft) / CELL_SIZE;
        int clickCursorY = (mouseY - gridTop) / CELL_SIZE;

        // Validate cursor is within grid bounds
        if (clickCursorX > 8 || clickCursorY > 8
            || clickCursorX < 0
            || clickCursorY < 0
            || mouseX < gridLeft
            || mouseY < gridTop) {
            return;
        }

        // Left-click = Select piece
        if (button == 0) {
            selectedX = clickCursorX;
            selectedY = clickCursorY;
            return;
        }

        // Right-click on grid
        if (button == 1) {
            vazkii.psi.api.spell.SpellPiece existingPiece = editingSpell.grid.gridData[clickCursorX][clickCursorY];

            // Right-click + Shift = Delete piece
            if (isShiftKeyDown() && existingPiece != null) {
                editingSpell.grid.gridData[clickCursorX][clickCursorY] = null;
                syncSpellToServer(); // Sync deletion to server
                return;
            }

            // Right-click on any cell (empty or occupied) = Open piece selection to place/replace
            openPieceSelection(clickCursorX, clickCursorY);
            return;
        }
    }

    /**
     * Handle clicks on side configuration buttons.
     * Returns true if a button was clicked.
     */
    private boolean handleSideConfigClick(int mouseX, int mouseY, int button) {
        if (button != 0) {
            return false; // Only handle left-clicks
        }

        if (selectedX < 0 || selectedY < 0) {
            return false;
        }

        vazkii.psi.api.spell.SpellPiece piece = editingSpell.grid.gridData[selectedX][selectedY];
        if (piece == null || piece.params.isEmpty()) {
            return false;
        }

        // Check each parameter's buttons
        int paramIndex = 0;
        for (String paramName : piece.params.keySet()) {
            vazkii.psi.api.spell.SpellParam<?> param = piece.params.get(paramName);

            int panelX = guiLeft - 75;
            int panelY = guiTop + 70 + paramIndex * 26;

            // Check each side button
            for (vazkii.psi.api.spell.SpellParam.Side side : vazkii.psi.api.spell.SpellParam.Side.values()) {
                if (side == vazkii.psi.api.spell.SpellParam.Side.OFF && !param.canDisable) {
                    continue;
                }

                int btnX = panelX + 58 + side.offx * 8;
                int btnY = panelY + side.offy * 8;

                if (mouseX >= btnX && mouseX < btnX + 8 && mouseY >= btnY && mouseY < btnY + 8) {
                    // Button clicked! Set this param to this side
                    piece.paramSides.put(param, side);
                    syncSpellToServer();
                    return true;
                }
            }

            paramIndex++;
        }

        return false;
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
