package com.mndk.bteterrarenderer.gui.sidebar;

import net.minecraft.client.gui.Gui;

// TODO Why doesn't this work
public class SidebarHorizontalLine extends GuiSidebarElement {

    private final int thickness;
    private final int color;

    public SidebarHorizontalLine(int thickness, int color) {
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public int getHeight() {
        return thickness;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, parent.elementWidth.get(), thickness + 1, color);
    }

    @Override protected void init() {}
    @Override public void onWidthChange(int newWidth) {}
    @Override public void updateScreen() {}
    @Override public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
    @Override public void keyTyped(char key, int keyCode) {}
    @Override public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    @Override public void mouseReleased(int mouseX, int mouseY, int state) {}
}
