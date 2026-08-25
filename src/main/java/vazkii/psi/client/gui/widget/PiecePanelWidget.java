package vazkii.psi.client.gui.widget;

import vazkii.psi.client.gui.GuiProgrammer;

/**
 * Piece picker boundary matching modern Psi's PiecePanelWidget. Its rendering
 * and event implementation is intentionally delegated during this migration,
 * so the legacy screen can be split without changing input timing.
 */
public final class PiecePanelWidget {

    private final GuiProgrammer parent;

    public PiecePanelWidget(GuiProgrammer parent) {
        this.parent = parent;
    }

    public boolean isOpen() {
        return parent.isPieceSelectionOpen();
    }

    public void render(int mouseX, int mouseY) {
        if (isOpen()) parent.renderPieceSelectionPanel(mouseX, mouseY);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!isOpen()) return false;
        parent.handlePieceSelectionPanelClick(mouseX, mouseY, button);
        return true;
    }

    public void open(int gridX, int gridY) {
        parent.openPieceSelectionPanel(gridX, gridY);
    }

    public void close() {
        parent.closePieceSelectionPanel();
    }
}
