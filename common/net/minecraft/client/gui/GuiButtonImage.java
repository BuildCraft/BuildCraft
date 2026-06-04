// STUB(R.Chen): Minecraft 1.12 GuiButtonImage — compile shim.
package net.minecraft.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiButtonImage extends ButtonWidget {
    public GuiButtonImage(int id, int x, int y, int width, int height, int textureX, int textureY, int textureHoveredOffset, Identifier texture) {
        super(x, y, width, height, Text.empty(), b -> {},
              ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
    }
}
