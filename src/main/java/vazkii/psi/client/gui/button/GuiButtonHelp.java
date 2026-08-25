package vazkii.psi.client.gui.button;

/** Geometry for the programmer help button. */
public final class GuiButtonHelp {

    public int getY(int guiTop, int ySize) {
        return guiTop + ySize - 48;
    }

    public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop, int xSize, int ySize) {
        int x = guiLeft + xSize + 2;
        int y = getY(guiTop, ySize);
        return mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 12;
    }
}
