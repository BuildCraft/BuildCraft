// STUB(R.Chen): Minecraft 1.12 GuiTextField → 1.20.1 TextFieldWidget.
package net.minecraft.client.gui;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class GuiTextField extends TextFieldWidget {
    public GuiTextField(int id, TextRenderer renderer, int x, int y, int width, int height) {
        super(renderer, x, y, width, height, Text.empty());
    }

    public GuiTextField(TextRenderer renderer, int x, int y, int width, int height) {
        super(renderer, x, y, width, height, Text.empty());
    }
}
