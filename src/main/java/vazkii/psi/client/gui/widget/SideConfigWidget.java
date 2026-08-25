package vazkii.psi.client.gui.widget;

import vazkii.psi.client.gui.GuiProgrammer;

/** Modern-Psi side configuration widget boundary for the 1.7.10 screen. */
public final class SideConfigWidget {

    private final GuiProgrammer parent;

    public SideConfigWidget(GuiProgrammer parent) {
        this.parent = parent;
    }

    public void render(int mouseX, int mouseY) {
        parent.renderSideConfigWidget(mouseX, mouseY);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return parent.handleSideConfigWidgetClick(mouseX, mouseY, button);
    }
}
