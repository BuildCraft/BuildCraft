// STUB(R.Chen): Minecraft 1.12 GuiButton → 1.20.1 ButtonWidget.
package net.minecraft.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class GuiButton extends ButtonWidget {
    public boolean enabled = true;
    public boolean visible = true;
    public int id;

    public GuiButton(int id, int x, int y, int width, int height, String text) {
        super(x, y, width, height, Text.literal(text), b -> {},
              ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.id = id;
    }

    public boolean isMouseOver(double x, double y) {
        return this.isHovered();
    }
}
