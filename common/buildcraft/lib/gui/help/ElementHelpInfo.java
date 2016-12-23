package buildcraft.lib.gui.help;

import java.util.List;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.elem.GuiElementContainer;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;

public class ElementHelpInfo {
    public final String title;
    public final IGuiArea position;
    public final int colour;
    public final String[] localeKeys;

    public ElementHelpInfo(String identifier, IGuiArea position, int colour, String... localeKeys) {
        this.title = identifier;
        this.position = position;
        this.colour = colour;
        this.localeKeys = localeKeys;
    }

    public void addGuiElements(GuiElementContainer container) {
        GuiBC8<?> gui = container.gui;
        int y = 20;
        for (String key : localeKeys) {
            if (key == null) {
                y += container.gui.getFontRenderer().FONT_HEIGHT + 5;
                continue;
            }
            String localized = LocaleUtil.localize(key);
            List<String> lines = StringUtilBC.splitIntoLines(localized);

            for (String line : lines) {
                GuiElementText elemText = new GuiElementText(gui, container, new GuiRectangle(0, y, 0, 0), line, 0, true);
                container.add(elemText);
                y += elemText.getHeight() + 5;
            }
        }
    }
}
