package buildcraft.core.gui;

import buildcraft.core.gui.buttons.GuiBetterButton;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;

public class GuiTools {

    public static void drawCenteredString(FontRenderer fr, String s, int y) {
        drawCenteredString(fr, s, y, 176);
    }

    public static void drawCenteredString(FontRenderer fr, String s, int y, int guiWidth) {
        drawCenteredString(fr, s, y, guiWidth, 0x404040, false);
    }

    public static void drawCenteredString(FontRenderer fr, String s, int y, int guiWidth, int color, boolean shadow) {
        int sWidth = fr.getStringWidth(s);
        int sPos = guiWidth / 2 - sWidth / 2;
        fr.drawString(s, sPos, y, color, shadow);
    }

    public static void newButtonRowAuto(List buttonList, int xStart, int xSize, List<? extends GuiBetterButton> buttons) {
        int buttonWidth = 0;
        for (GuiBetterButton b : buttons) {
            buttonWidth += b.getWidth();
        }
        int remaining = xSize - buttonWidth;
        int spacing = remaining / (buttons.size() + 1);
        int pointer = 0;
        for (GuiBetterButton b : buttons) {
            pointer += spacing;
            b.xPosition = xStart + pointer;
            pointer += b.getWidth();
            buttonList.add(b);
        }
    }

    public static void newButtonRow(List buttonList, int xStart, int spacing, List<? extends GuiBetterButton> buttons) {
        int pointer = 0;
        for (GuiBetterButton b : buttons) {
            b.xPosition = xStart + pointer;
            pointer += b.getWidth() + spacing;
            buttonList.add(b);
        }
    }

}
