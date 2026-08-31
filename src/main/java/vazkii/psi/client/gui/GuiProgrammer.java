package vazkii.psi.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.client.core.helper.SharingHelper;
import vazkii.psi.client.gui.button.GuiButtonHelp;
import vazkii.psi.client.gui.button.GuiButtonIO;
import vazkii.psi.client.gui.widget.PiecePanelWidget;
import vazkii.psi.client.gui.widget.SideConfigWidget;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.lib.LibMisc;
import vazkii.psi.common.spell.constant.PieceConstantNumber;
import vazkii.psi.common.spell.other.PieceConnector;
import vazkii.psi.common.spell.other.PieceCrossConnector;

/**
 * 1.7.10 implementation of the modern Psi programmer screen. The historical
 * GuiSpellProgrammer name is retained as a thin compatibility wrapper.
 */
public class GuiProgrammer extends GuiScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation("psi", "textures/gui/programmer.png");
    private static final ResourceLocation CONNECTOR_LINES_TEXTURE = new ResourceLocation("psi", "textures/spell/connector_lines.png");

    private final ItemStack cadStack;
    private final TileProgrammer programmer;
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
    private net.minecraft.client.gui.GuiTextField commentField;
    private boolean commentEnabled;
    private final java.util.Deque<Spell> undoSteps = new java.util.ArrayDeque<Spell>();
    private final java.util.Deque<Spell> redoSteps = new java.util.ArrayDeque<Spell>();
    private vazkii.psi.api.spell.SpellPiece clipboard;
    private SpellCompilationException compilationError;
    private CompiledSpell compiledSpell;
    private final java.util.List<String> programmerTooltip = new java.util.ArrayList<String>();

    private int panelPage;
    private int panelCursor;
    private static final int PIECES_PER_PAGE = 25;
    private final PiecePanelWidget piecePanelWidget = new PiecePanelWidget(this);
    private final SideConfigWidget sideConfigWidget = new SideConfigWidget(this);
    private final GuiButtonHelp helpButton = new GuiButtonHelp();
    private final GuiButtonIO exportButton = new GuiButtonIO(true);
    private final GuiButtonIO importButton = new GuiButtonIO(false);

    public GuiProgrammer(ItemStack cadStack) {
        this.cadStack = cadStack;
        this.programmer = null;

        // Load spell from CAD if it has one
        this.editingSpell = ItemCAD.getSpell(cadStack);
        if (this.editingSpell == null) {
            // Create empty spell if CAD has no spell
            this.editingSpell = new Spell();
        }
    }

    /** Opens the existing editor against a world-owned programmer spell. */
    public GuiProgrammer(TileProgrammer programmer) {
        this.cadStack = null;
        this.programmer = programmer;
        this.editingSpell = programmer.spell == null ? new Spell() : programmer.spell;
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

        spellNameField = new net.minecraft.client.gui.GuiTextField(
            fontRendererObj,
            guiLeft + xSize - 130,
            guiTop + ySize - 14,
            120,
            10);
        spellNameField.setMaxStringLength(20);
        spellNameField.setEnableBackgroundDrawing(false);
        spellNameField.setText(editingSpell.name == null ? "" : editingSpell.name);
        // Match the modern programmer: the comment editor overlays the centre
        // of the grid and leaves room below it for the editing instructions.
        commentField = new net.minecraft.client.gui.GuiTextField(
            fontRendererObj,
            guiLeft,
            guiTop + ySize / 2 - 10,
            xSize,
            20);
        commentField.setMaxStringLength(500);
        recompileSpell();

        // GuiProgrammer's selected coordinates are static and therefore begin
        // at (0, 0). Mirroring that here makes keyboard navigation usable
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
        if (commentEnabled) commentField.updateCursorCounter();
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
        programmerTooltip.clear();
        drawProgrammerStatus(mouseX, mouseY);
        drawSpellCosts(mouseX, mouseY);

        // Calculate cursor position based on mouse
        cursorX = (mouseX - gridLeft) / CELL_SIZE;
        cursorY = (mouseY - gridTop) / CELL_SIZE;

        // Validate cursor is within grid bounds
        // The picker, not the side configuration widget, owns mouse input while
        // open. This is what lets the original retain grid hover while config
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

        // Draw hover after selection. The original uses the left half of the
        // hover sprite when both markers occupy the same cell, so both remain
        // legible instead of one completely obscuring the other.
        if (!pieceSelectionOpen && cursorX >= 0 && cursorY >= 0) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            // Hover overlay texture at (16, ySize) in programmer.png, size 16x16
            int hoverWidth = cursorX == selectedX && cursorY == selectedY ? 8 : 16;
            this.drawTexturedModalRect(
                gridLeft + cursorX * CELL_SIZE,
                gridTop + cursorY * CELL_SIZE,
                16,
                ySize,
                hoverWidth,
                16);
        }

        drawSelectedPieceLabel();
        if (commentEnabled) {
            commentField.drawTextBox();
            String commit = net.minecraft.client.resources.I18n.format("psimisc.enter_commit");
            String lineBreak = net.minecraft.client.resources.I18n.format("psimisc.semicolon_line");
            fontRendererObj.drawStringWithShadow(
                commit,
                guiLeft + (xSize - fontRendererObj.getStringWidth(commit)) / 2,
                commentField.yPosition + 24,
                0xFFFFFF);
            fontRendererObj.drawStringWithShadow(
                lineBreak,
                guiLeft + (xSize - fontRendererObj.getStringWidth(lineBreak)) / 2,
                commentField.yPosition + 34,
                0xFFFFFF);
        }

        // Keep config rendering ahead of tooltips. Tooltip rendering changes
        // GL state in 1.7.10; drawing the panel afterwards was the source of
        // its washed-out appearance while hovering a grid piece.
        sideConfigWidget.render(mouseX, mouseY);
        drawSpellNameField();
        drawProgrammerFooter();

        // Tooltip rendering in 1.7.10 leaves a dark blend state. Render the
        // right-side widgets first, as with the configuration panel.
        drawClipboardButtons(mouseX, mouseY);

        // Normal hover shows its piece. Holding Alt substitutes the selected
        // piece and pins the tooltip to that grid cell, as in 1.21.1.
        if (!pieceSelectionOpen) {
            int tooltipX = mouseX;
            int tooltipY = mouseY;
            vazkii.psi.api.spell.SpellPiece tooltipPiece = cursorX >= 0 && cursorY >= 0
                ? editingSpell.grid.gridData[cursorX][cursorY]
                : null;
            if (isAltHeld() && selectedX >= 0 && selectedY >= 0) {
                tooltipPiece = editingSpell.grid.gridData[selectedX][selectedY];
                tooltipX = gridLeft + selectedX * CELL_SIZE + 10;
                tooltipY = gridTop + selectedY * CELL_SIZE + 8;
            }
            if (tooltipPiece != null) {
                if (tooltipPiece.comment != null && !tooltipPiece.comment.isEmpty()) {
                    java.util.List<String> commentLines = new java.util.ArrayList<String>();
                    for (String line : tooltipPiece.comment.split(";")) commentLines.add(line);
                    // GuiScreen's tooltip renderer supplies the correct framed
                    // box; move this one above the regular piece tooltip.
                    this.drawHoveringText(
                        commentLines,
                        tooltipX,
                        tooltipY - 9 - commentLines.size() * 10,
                        fontRendererObj);
                }
                this.drawHoveringText(buildPieceTooltip(tooltipPiece), tooltipX, tooltipY, fontRendererObj);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (!programmerTooltip.isEmpty() && !pieceSelectionOpen) {
            this.drawHoveringText(programmerTooltip, mouseX, mouseY, fontRendererObj);
        }

        // Draw piece selection overlay LAST (on top of everything)
        piecePanelWidget.render(mouseX, mouseY);

    }

    private void drawCompilationError() {
        if (compilationError == null || compilationError.x < 0 || compilationError.y < 0) return;
        fontRendererObj.drawStringWithShadow(
            "!!",
            gridLeft + compilationError.x * CELL_SIZE + 12,
            gridTop + compilationError.y * CELL_SIZE + 8,
            0xFF0000);
    }

    /** Draws the compiler status and the player's active Casting Assistant Device. */
    private void drawProgrammerStatus(int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);
        this.drawTexturedModalRect(guiLeft - 48, guiTop + 5, xSize, 0, 48, 30);

        int statusX = guiLeft - 16;
        int statusY = guiTop + 13;
        this.drawTexturedModalRect(statusX, statusY, compilationError == null ? 0 : 12, ySize + 28, 12, 12);
        if (mouseX >= statusX && mouseX < statusX + 12 && mouseY >= statusY && mouseY < statusY + 12) {
            if (compilationError == null) {
                programmerTooltip.add("\u00a7a" + net.minecraft.client.resources.I18n.format("psimisc.compiled"));
            } else {
                programmerTooltip.add("\u00a7c" + net.minecraft.client.resources.I18n.format("psimisc.errored"));
                programmerTooltip
                    .add("\u00a77" + net.minecraft.client.resources.I18n.format(compilationError.getMessage()));
                if (compilationError.x >= 0 && compilationError.y >= 0) {
                    programmerTooltip.add(
                        "\u00a77" + net.minecraft.client.resources.I18n
                            .format("psi.spellerror.position", compilationError.x + 1, compilationError.y + 1));
                }
            }
        }

        ItemStack cad = getCastingAssistant();
        if (cad != null) {
            int cadX = guiLeft - 42;
            int cadY = guiTop + 12;
            itemRender.renderItemAndEffectIntoGUI(fontRendererObj, this.mc.getTextureManager(), cad, cadX, cadY);
            if (mouseX >= cadX && mouseX < cadX + 16 && mouseY >= cadY && mouseY < cadY + 16) {
                programmerTooltip.addAll(cad.getTooltip(this.mc.thePlayer, false));
            }
        }
    }

    /** Draws modern Psi's spell-stat readout, using the assembled CAD's limits. */
    private void drawSpellCosts(int mouseX, int mouseY) {
        if (compiledSpell == null) return;
        ItemStack cad = getCastingAssistant();
        int statX = guiLeft + xSize + 3;
        int i = 0;
        for (EnumSpellStat stat : EnumSpellStat.values()) {
            int statY = guiTop + 20 + i * 20;
            int value = compiledSpell.metadata.getStat(stat);
            String limitName = cadStatName(stat);
            // No equipped CAD means no capacity; the creative CAD intentionally
            // reports an unlimited capacity, just like the modern UI.
            int limit = limitName == null ? -1
                : cad == null ? 0 : cad.getItem() instanceof ItemCreativeCAD ? -1 : ItemCAD.getStat(cad, limitName);
            String text = stat == EnumSpellStat.COST ? String.valueOf(value)
                : value + "/" + (limit < 0 ? "\u221e" : limit);
            int colour = limitName != null && limit >= 0 && value > limit ? 0xFF6666 : 0xFFFFFF;

            GL11.glColor4f(1F, 1F, 1F, 1F);
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            this.drawTexturedModalRect(statX, statY, (stat.ordinal() + 1) * 12, ySize + 16, 12, 12);
            fontRendererObj.drawString(text, statX + 16, statY + 2, colour);
            if (mouseX >= statX && mouseX < statX + 12 && mouseY >= statY && mouseY < statY + 12) {
                programmerTooltip.add("\u00a7b" + net.minecraft.client.resources.I18n.format(stat.getName()));
                programmerTooltip.add("\u00a77" + net.minecraft.client.resources.I18n.format(stat.getDesc()));
            }
            i++;
        }
    }

    private ItemStack getCastingAssistant() {
        if (cadStack != null
            && (cadStack.getItem() instanceof ItemCAD || cadStack.getItem() instanceof ItemCreativeCAD))
            return cadStack;
        ItemStack held = this.mc.thePlayer == null ? null : this.mc.thePlayer.getHeldItem();
        return held != null && (held.getItem() instanceof ItemCAD || held.getItem() instanceof ItemCreativeCAD) ? held
            : null;
    }

    private String cadStatName(EnumSpellStat stat) {
        switch (stat) {
            case COMPLEXITY:
                return "Complexity";
            case POTENCY:
                return "Potency";
            case PROJECTION:
                return "Projection";
            case BANDWIDTH:
                return "Bandwidth";
            default:
                return null;
        }
    }

    private void drawSpellNameField() {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        fontRendererObj.drawString(
            net.minecraft.client.resources.I18n.format("psimisc.name"),
            guiLeft + padLeft,
            spellNameField.yPosition + 1,
            0xFFFFFF);
        spellNameField.drawTextBox();
    }

    /** Status text below the canvas, positioned and coloured like modern Psi. */
    private void drawProgrammerFooter() {
        if (selectedX < 0 || selectedY < 0) return;
        String coords;
        if (cursorX >= 0 && cursorY >= 0) {
            coords = net.minecraft.client.resources.I18n
                .format("psimisc.programmer_coords", selectedX + 1, selectedY + 1, cursorX + 1, cursorY + 1);
        } else {
            coords = net.minecraft.client.resources.I18n
                .format("psimisc.programmer_coords_no_cursor", selectedX + 1, selectedY + 1);
        }
        int topY = guiTop - 22;
        int coordsY = topY + ySize + 24;
        fontRendererObj.drawString(coords, guiLeft + 4, coordsY, 0x44FFFFFF);
        String version = "Psi " + LibMisc.VERSION;
        fontRendererObj.drawStringWithShadow(
            version,
            guiLeft + (xSize - fontRendererObj.getStringWidth(version)) / 2,
            coordsY + fontRendererObj.FONT_HEIGHT + 5,
            0x44FFFFFF);
    }

    private void recompileSpell() {
        try {
            compiledSpell = new SpellCompiler().compile(editingSpell);
            compilationError = null;
        } catch (SpellCompilationException error) {
            compiledSpell = null;
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

        drawConnectorLines(piece, screenX, screenY);

        // The 6x6 speech-bubble marker is the same programmer-atlas sprite
        // used by modern Psi, positioned slightly beyond the top-left edge.
        if (piece.comment != null && !piece.comment.isEmpty()) {
            this.mc.getTextureManager()
                .bindTexture(TEXTURE);
            this.drawTexturedModalRect(screenX - 2, screenY - 2, 150, 184, 6, 6);
        }

        if (piece instanceof PieceConstantNumber) {
            String value = ((PieceConstantNumber) piece).getDisplayValue();
            if (value.length() > 5) {
                value = value.substring(0, 5);
            }
            int x = screenX + (CELL_SIZE - fontRendererObj.getStringWidth(value)) / 2;
            fontRendererObj.drawStringWithShadow(value, x, screenY + 5, 0xFFFFFF);
        }
    }

    /** Draws the dynamic connector strokes omitted from the static 16x16 icons. */
    private void drawConnectorLines(vazkii.psi.api.spell.SpellPiece piece, int screenX, int screenY) {
        if (piece instanceof PieceConnector) {
            PieceConnector connector = (PieceConnector) piece;
            drawConnectorLine(screenX, screenY, connector.getRedirectionSide(), 0xFFFFFF);
            if (connector.isInGrid) {
                for (vazkii.psi.api.spell.SpellParam.Side side : vazkii.psi.api.spell.SpellParam.Side.values()) {
                    if (!side.isEnabled()) continue;
                    vazkii.psi.api.spell.SpellPiece neighbor = editingSpell.grid.getPieceAtSideSafely(piece.x, piece.y, side);
                    if (neighbor != null && neighbor.isInputSide(side.getOpposite())) drawConnectorLine(screenX, screenY, side, 0xFFFFFF);
                }
            }
        } else if (piece instanceof PieceCrossConnector) {
            PieceCrossConnector connector = (PieceCrossConnector) piece;
            vazkii.psi.api.spell.SpellParam.Side[] sides = connector.getLineSides();
            int[] colors = connector.getLineColors();
            for (int i = 0; i < sides.length; i++) drawConnectorLine(screenX, screenY, sides[i], colors[i]);
        }
    }

    /** Draws one 16x16 connector-line quadrant, using the modern connector_lines texture layout. */
    private void drawConnectorLine(int x, int y, vazkii.psi.api.spell.SpellParam.Side side, int color) {
        if (side == null || !side.isEnabled()) return;
        float minU = 0F;
        float minV = 0F;
        switch (side) {
            case LEFT: minU = .5F; break;
            case TOP: minV = .5F; break;
            case BOTTOM: minU = .5F; minV = .5F; break;
            default: break;
        }
        float maxU = minU + .5F;
        float maxV = minV + .5F;
        this.mc.getTextureManager().bindTexture(CONNECTOR_LINES_TEXTURE);
        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
        tess.addVertexWithUV(x, y + 16, zLevel, minU, maxV);
        tess.addVertexWithUV(x + 16, y + 16, zLevel, maxU, maxV);
        tess.addVertexWithUV(x + 16, y, zLevel, maxU, minV);
        tess.addVertexWithUV(x, y, zLevel, minU, minV);
        tess.draw();
        GL11.glColor4f(1F, 1F, 1F, 1F);
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

        if (commentEnabled) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                closeComment(true);
            } else if (keyCode == Keyboard.KEY_ESCAPE) {
                closeComment(false);
            } else {
                commentField.textboxKeyTyped(typedChar, keyCode);
            }
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
        if (selectedPiece != null && !selectedPiece.params.isEmpty()
            && keyCode >= Keyboard.KEY_1
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

        // ESC key closes the GUI or piece selection
        if (keyCode == Keyboard.KEY_ESCAPE) {
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
            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                if (isCtrlKeyDown() && isShiftKeyDown()) {
                    if (!editingSpell.grid.isEmpty()) {
                        pushState();
                        editingSpell = new Spell();
                        spellNameField.setText("");
                        syncSpellToServer();
                    }
                } else if (selectedPiece != null) {
                    pushState();
                    editingSpell.grid.gridData[selectedX][selectedY] = null;
                    syncSpellToServer();
                }
                return;
            } else if (keyCode == Keyboard.KEY_TAB) {
                spellNameField.setFocused(true);
                return;
            } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                piecePanelWidget.open(selectedX, selectedY);
                return;
            } else if (isCtrlKeyDown() && keyCode == Keyboard.KEY_Z) {
                undo();
                return;
            } else if (isCtrlKeyDown() && keyCode == Keyboard.KEY_Y) {
                redo();
                return;
            } else if (isCtrlKeyDown() && keyCode == Keyboard.KEY_C && selectedPiece != null) {
                clipboard = copyPiece(selectedPiece);
                return;
            } else if (isCtrlKeyDown() && keyCode == Keyboard.KEY_X && selectedPiece != null) {
                clipboard = copyPiece(selectedPiece);
                pushState();
                editingSpell.grid.gridData[selectedX][selectedY] = null;
                syncSpellToServer();
                return;
            } else if (isCtrlKeyDown() && keyCode == Keyboard.KEY_V && clipboard != null) {
                vazkii.psi.api.spell.SpellPiece copy = copyPiece(clipboard);
                if (copy != null) {
                    pushState();
                    copy.x = selectedX;
                    copy.y = selectedY;
                    copy.isInGrid = true;
                    editingSpell.grid.gridData[selectedX][selectedY] = copy;
                    syncSpellToServer();
                }
                return;
            } else if (isCtrlKeyDown() && keyCode == Keyboard.KEY_D && selectedPiece != null) {
                openComment(selectedPiece);
                return;
            } else if (isCtrlKeyDown() && isShiftKeyDown() && isAltHeld() && keyCode == Keyboard.KEY_G) {
                shareSpell(false);
                return;
            } else if (isCtrlKeyDown() && isShiftKeyDown() && isAltHeld() && keyCode == Keyboard.KEY_R) {
                shareSpell(true);
                return;
            } else if (keyCode == Keyboard.KEY_UP) {
                if (isCtrlKeyDown()) {
                    shiftOrTransform(vazkii.psi.api.spell.SpellParam.Side.TOP);
                    return;
                }
                if (selectedPiece != null
                    && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.TOP)) return;
                moveSelection(0, -1);
            } else if (keyCode == Keyboard.KEY_DOWN) {
                if (isCtrlKeyDown()) {
                    shiftOrTransform(vazkii.psi.api.spell.SpellParam.Side.BOTTOM);
                    return;
                }
                if (selectedPiece != null
                    && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.BOTTOM)) return;
                moveSelection(0, 1);
            } else if (keyCode == Keyboard.KEY_LEFT) {
                if (isCtrlKeyDown()) {
                    shiftOrTransform(vazkii.psi.api.spell.SpellParam.Side.LEFT);
                    return;
                }
                if (selectedPiece != null
                    && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.LEFT)) return;
                moveSelection(-1, 0);
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                if (isCtrlKeyDown()) {
                    shiftOrTransform(vazkii.psi.api.spell.SpellParam.Side.RIGHT);
                    return;
                }
                if (selectedPiece != null
                    && onSideButtonKeybind(selectedPiece, param, vazkii.psi.api.spell.SpellParam.Side.RIGHT)) return;
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

    private void pushState() {
        undoSteps.push(copySpell(editingSpell));
        redoSteps.clear();
    }

    private Spell copySpell(Spell spell) {
        NBTTagCompound tag = new NBTTagCompound();
        spell.writeToNBT(tag);
        return Spell.readFromNBT(tag);
    }

    private vazkii.psi.api.spell.SpellPiece copyPiece(vazkii.psi.api.spell.SpellPiece piece) {
        NBTTagCompound tag = new NBTTagCompound();
        piece.writeToNBT(tag);
        return vazkii.psi.api.spell.SpellPiece.createFromNBT(editingSpell, tag);
    }

    private void undo() {
        if (undoSteps.isEmpty()) return;
        redoSteps.push(copySpell(editingSpell));
        editingSpell = undoSteps.pop();
        spellNameField.setText(editingSpell.name == null ? "" : editingSpell.name);
        syncSpellToServer();
    }

    private void redo() {
        if (redoSteps.isEmpty()) return;
        undoSteps.push(copySpell(editingSpell));
        editingSpell = redoSteps.pop();
        spellNameField.setText(editingSpell.name == null ? "" : editingSpell.name);
        syncSpellToServer();
    }

    private void shiftOrTransform(vazkii.psi.api.spell.SpellParam.Side side) {
        if (isShiftKeyDown()) {
            if (editingSpell.grid.isEmpty()) return;
            pushState();
            if (side == vazkii.psi.api.spell.SpellParam.Side.LEFT) editingSpell.grid.rotate(false);
            else if (side == vazkii.psi.api.spell.SpellParam.Side.RIGHT) editingSpell.grid.rotate(true);
            else editingSpell.grid.mirrorVertical();
            syncSpellToServer();
        } else if (editingSpell.grid.shift(side, false)) {
            pushState();
            editingSpell.grid.shift(side, true);
            syncSpellToServer();
        }
    }

    private void openComment(vazkii.psi.api.spell.SpellPiece piece) {
        commentEnabled = true;
        commentField.setText(piece.comment == null ? "" : piece.comment);
        commentField.setFocused(true);
    }

    private void closeComment(boolean save) {
        vazkii.psi.api.spell.SpellPiece piece = getSelectedPiece();
        if (save && piece != null) {
            pushState();
            piece.comment = commentField.getText();
            syncSpellToServer();
        }
        commentField.setFocused(false);
        commentEnabled = false;
    }

    private void shareSpell(boolean reddit) {
        try {
            NBTTagCompound tag = new NBTTagCompound();
            editingSpell.writeToNBT(tag);
            String screenshot = SharingHelper.takeScreenshot();
            if (reddit) SharingHelper.uploadAndShare(editingSpell.name, tag.toString(), screenshot);
            else SharingHelper.uploadAndOpen(editingSpell.name, tag.toString(), screenshot);
        } catch (Exception ignored) {
            // Screenshot capture can fail while the display is being recreated.
        }
    }

    private boolean onSideButtonKeybind(vazkii.psi.api.spell.SpellPiece piece, int paramIndex,
        vazkii.psi.api.spell.SpellParam.Side side) {
        if (paramIndex < 0 || paramIndex >= piece.params.size()) return false;
        vazkii.psi.api.spell.SpellParam<?> param = new java.util.ArrayList<vazkii.psi.api.spell.SpellParam<?>>(
            piece.params.values()).get(paramIndex);
        if (side == vazkii.psi.api.spell.SpellParam.Side.OFF && !param.canDisable) return false;
        if (side != vazkii.psi.api.spell.SpellParam.Side.OFF && piece.paramSides.get(param) == side) {
            if (!param.canDisable) return false;
            side = vazkii.psi.api.spell.SpellParam.Side.OFF;
        }
        pushState();
        piece.paramSides.put(param, side);
        syncSpellToServer();
        return true;
    }

    private void drawSelectedPieceLabel() {
        vazkii.psi.api.spell.SpellPiece piece = getSelectedPiece();
        if (piece == null || pieceSelectionOpen) return;
        String name = net.minecraft.client.resources.I18n.format(piece.getUnlocalizedName());
        fontRendererObj.drawStringWithShadow(
            name,
            guiLeft + (xSize - fontRendererObj.getStringWidth(name)) / 2,
            guiTop - 22,
            0xFFFFFF);
    }

    private java.util.List<String> buildPieceTooltip(vazkii.psi.api.spell.SpellPiece piece) {
        java.util.List<String> tooltip = new java.util.ArrayList<String>();
        piece.getTooltip(tooltip);
        if (isShiftKeyDown()) {
            tooltip.add("");
            tooltip.add("Output \u00a76" + piece.getEvaluationTypeString());
            for (vazkii.psi.api.spell.SpellParam<?> param : piece.params.values()) {
                tooltip.add(
                    (param.canDisable ? "[Input] " : " Input  ") + "\u00a7e"
                        + net.minecraft.client.resources.I18n.format(param.name)
                        + "\u00a7e ["
                        + param.getRequiredTypeString()
                        + "]");
            }
        } else {
            tooltip.add(net.minecraft.client.resources.I18n.format("psimisc.shift_for_info"));
        }
        if (piece.hasStatLabels()) {
            if (isCtrlKeyDown()) {
                tooltip.add("");
                for (EnumSpellStat stat : EnumSpellStat.values()) {
                    vazkii.psi.api.spell.StatLabel label = piece.getDefinedStatLabel(stat);
                    if (label != null) {
                        tooltip.add(net.minecraft.client.resources.I18n.format(stat.getName()) + ":");
                        tooltip.add(" \u00a7e" + label.toString());
                    }
                }
            } else {
                tooltip.add(net.minecraft.client.resources.I18n.format("psimisc.ctrl_for_stats"));
            }
        }
        return tooltip;
    }

    private String colorModifierKeys(String text) {
        return text.replace("SHIFT", "\u00a7bSHIFT\u00a77")
            .replace("Shift", "\u00a7bShift\u00a77")
            .replace("CTRL", "\u00a7bCTRL\u00a77")
            .replace("Ctrl", "\u00a7bCtrl\u00a77");
    }

    /**
     * Open the piece selection overlay at the given grid coordinates.
     */
    public void openPieceSelectionPanel(int gridX, int gridY) {
        openPieceSelection(gridX, gridY);
    }

    public void closePieceSelectionPanel() {
        closePieceSelection();
    }

    public void renderPieceSelectionPanel(int mouseX, int mouseY) {
        drawPieceSelectionOverlay(mouseX, mouseY);
    }

    public void handlePieceSelectionPanelClick(int mouseX, int mouseY, int button) {
        handlePieceSelectionClick(mouseX, mouseY, button);
    }

    public boolean isPieceSelectionOpen() {
        return pieceSelectionOpen;
    }

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
            drawPiecePickerOverlay(pieceId, btnX, btnY, buttonSize);

            // Mouse hover uses the exact same sprite as the spell grid, over
            // the icon. The blue keyboard marker above remains behind it.
            if (hovered) {
                this.mc.getTextureManager()
                    .bindTexture(TEXTURE);
                this.drawTexturedModalRect(btnX, btnY, 16, ySize, 16, 16);
            }

            // Store tooltip for rendering last
            if (hovered) {
                vazkii.psi.api.spell.SpellPiece preview = vazkii.psi.common.spell.SpellPieceRegistry
                    .create(pieceId, editingSpell);
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
        fontRendererObj.drawString(
            pageText,
            panelX + (panelWidth - fontRendererObj.getStringWidth(pageText)) / 2,
            panelY + panelHeight - 12,
            0xFFFFFF);

        // Draw tooltip LAST so it renders on top of everything
        if (hoveredPieceTooltip != null) {
            this.drawHoveringText(hoveredPieceTooltip, mouseX, mouseY, fontRendererObj);
        }
    }

    /** Draws dynamic piece content that is not part of the static atlas icon. */
    private void drawPiecePickerOverlay(String pieceId, int x, int y, int size) {
        if (!"psi:constant_number".equals(pieceId)) return;

        String value = searchField.getText();
        if (!isNumber(value)) value = "0";
        if (value.length() > 5) value = value.substring(0, 5);
        // Picker buttons are 18x18, but the number-piece texture is a 16x16
        // canvas. Align dynamic text to that canvas, as drawAdditional does
        // in modern Psi, rather than to the surrounding button cell.
        PieceTextureAtlas.UVCoords icon = PieceTextureAtlas.getInstance()
            .getUV(pieceId);
        int iconWidth = icon == null ? size : icon.width;
        fontRendererObj.drawString(value, x + (iconWidth - fontRendererObj.getStringWidth(value)) / 2, y + 4, 0xFFFFFF);
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
                newPiece.isInGrid = true;

                // A first trick is a natural, useful name for a new spell. Never
                // replace a player-entered name, including one restored by undo.
                if (newPiece instanceof PieceTrick && (editingSpell.name == null || editingSpell.name.trim()
                    .isEmpty())) {
                    String generatedName = net.minecraft.client.resources.I18n.format(newPiece.getUnlocalizedName())
                        .replaceFirst("^Trick:\\s*", "");
                    editingSpell.name = generatedName.length() > 20 ? generatedName.substring(0, 20) : generatedName;
                    spellNameField.setText(editingSpell.name);
                }

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
        String searchText = searchField.getText()
            .toLowerCase(java.util.Locale.ROOT)
            .trim();
        java.util.List<String> pieces = new java.util.ArrayList<String>();
        for (String piece : vazkii.psi.common.spell.SpellPieceRegistry.getRegisteredIds()) {
            String name = formatPieceName(piece).toLowerCase(java.util.Locale.ROOT);
            if (searchText.isEmpty() || piece.toLowerCase(java.util.Locale.ROOT)
                .contains(searchText) || name.contains(searchText)) {
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

                    if (!side.isEnabled() || param.getArrowType() == vazkii.psi.api.spell.SpellParam.ArrowType.NONE) {
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
                    addParamArrowToBatch(tess, x, y, side, param.color, param.getArrowType(), percent);
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
            if (param.getArrowType() != vazkii.psi.api.spell.SpellParam.ArrowType.NONE
                && piece.paramSides.get(param) == piece.paramSides.get(target)) {
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
            if (param.getArrowType() != vazkii.psi.api.spell.SpellParam.ArrowType.NONE && piece.paramSides.get(param) == side) {
                count++;
            }
        }
        return count;
    }

    /**
     * Add a single parameter arrow to the Tessellator batch.
     */
    private void addParamArrowToBatch(net.minecraft.client.renderer.Tessellator tess, int pieceX, int pieceY,
        vazkii.psi.api.spell.SpellParam.Side side, int color, vazkii.psi.api.spell.SpellParam.ArrowType arrowType, float percent) {
        if (arrowType == vazkii.psi.api.spell.SpellParam.ArrowType.NONE) return;
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

        if (arrowType == vazkii.psi.api.spell.SpellParam.ArrowType.OUT) side = side.getOpposite();

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
        if (editingSpell != null && (cadStack != null || programmer != null)) {
            recompileSpell();
            NBTTagCompound spellNBT = new NBTTagCompound();
            editingSpell.writeToNBT(spellNBT);

            // Send packet to server
            if (programmer != null) {
                programmer.setSpell(editingSpell);
                vazkii.psi.common.network.PacketHandler.INSTANCE.sendToServer(
                    new vazkii.psi.common.network.PacketProgrammerSpellUpdate(
                        programmer.xCoord,
                        programmer.yCoord,
                        programmer.zCoord,
                        editingSpell));
            } else {
                vazkii.psi.common.network.PacketHandler.INSTANCE
                    .sendToServer(new vazkii.psi.common.network.PacketSpellUpdate(editingSpell));
            }

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
        int helpY = helpButton.getY(guiTop, ySize);
        int exportY = exportButton.getY(guiTop, ySize);
        int importY = importButton.getY(guiTop, ySize);
        boolean overHelp = helpButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);
        boolean overExport = exportButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);
        boolean overImport = importButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);

        // Tooltips in 1.7.10 alter GL colour state. Reset it explicitly because
        // these controls must never inherit a dimmed colour from grid/picker UI.
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.mc.getTextureManager()
            .bindTexture(TEXTURE);
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
                    if (!line.equals(key)) tooltip.add(colorModifierKeys(line));
                }
            } else {
                tooltip.add("\u00a77Hold \u00a7bSHIFT\u00a77 for more info");
            }
            this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
        } else if (overExport || overImport) {
            java.util.List<String> tooltip = new java.util.ArrayList<String>();
            tooltip.add(overExport ? "\u00a7cExport to Clipboard" : "\u00a79Import from Clipboard");
            tooltip.add("\u00a77(Must be holding \u00a7bSHIFT\u00a77)");
            this.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
        }
    }

    private boolean handleClipboardButtonClick(int mouseX, int mouseY) {
        int x = guiLeft + xSize + 2;
        int helpY = helpButton.getY(guiTop, ySize);
        int exportY = exportButton.getY(guiTop, ySize);
        int importY = importButton.getY(guiTop, ySize);
        if (helpButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize)) {
            return true; // The original help button is intentionally informational.
        }
        if (!exportButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize)
            && !importButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize)) {
            return false;
        }
        if (!isShiftKeyDown()) {
            return true;
        }
        if (exportButton.isMouseOver(mouseX, mouseY, guiLeft, guiTop, xSize, ySize)) {
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
            if (isOver(
                mouseX,
                mouseY,
                spellNameField.xPosition,
                spellNameField.yPosition,
                spellNameField.width,
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
            piecePanelWidget.mouseClicked(mouseX, mouseY, button);
            return;
        }

        // Handle side config button clicks
        if (sideConfigWidget.mouseClicked(mouseX, mouseY, button)) {
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
            piecePanelWidget.open(clickCursorX, clickCursorY);
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

    public void renderSideConfigWidget(int mouseX, int mouseY) {
        drawSideConfigPanel(mouseX, mouseY);
    }

    public boolean handleSideConfigWidgetClick(int mouseX, int mouseY, int button) {
        return handleSideConfigClick(mouseX, mouseY, button);
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
