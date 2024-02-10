package com.mndk.bteterrarenderer.core.gui.sidebar.checkbox;

import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.gui.widget.CheckBoxWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

public class SidebarCheckBox extends GuiSidebarElement {

    private CheckBoxWidgetCopy checkBox;
    private final String suffixText;
    private final PropertyAccessor<Boolean> propertyAccessor;

    public SidebarCheckBox(PropertyAccessor<Boolean> propertyAccessor, String suffixText) {
        this.propertyAccessor = propertyAccessor;
        this.suffixText = suffixText;
    }

    @Override
    public int getPhysicalHeight() {
        return 20;
    }

    @Override
    protected void init() {
        this.checkBox = new CheckBoxWidgetCopy(0, 0, this.getWidth(), suffixText, propertyAccessor.get());
    }

    @Override
    public void onWidthChange() {
        this.checkBox.setWidth(this.getWidth());
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        return this.checkBox.mouseHovered(mouseX, mouseY, partialTicks, mouseHidden);
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        this.checkBox.drawComponent(drawContextWrapper);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if(this.checkBox.mousePressed(mouseX, mouseY, mouseButton)) {
            propertyAccessor.set(this.checkBox.isChecked());
            return true;
        }
        return false;
    }
}