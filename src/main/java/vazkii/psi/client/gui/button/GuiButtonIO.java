package vazkii.psi.client.gui.button;

/** Geometry and identity for the programmer's import/export button. */
public final class GuiButtonIO {

    public final boolean export;

    public GuiButtonIO(boolean export) {
        this.export = export;
    }

    public int getY(int guiTop, int ySize) {
        return guiTop + ySize - (export ? 32 : 16);
    }

    public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop, int xSize, int ySize) {
        int x = guiLeft + xSize + 2;
        int y = getY(guiTop, ySize);
        return mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 12;
    }
}
