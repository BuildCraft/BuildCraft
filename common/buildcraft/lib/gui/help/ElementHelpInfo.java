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
    public final String identifier;
    public final IGuiArea position;
    public final int colour;
    public final String localeKey;

    public ElementHelpInfo(String identifier, IGuiArea position, int colour, String localeKey) {
        this.identifier = identifier;
        this.position = position;
        this.colour = colour;
        this.localeKey = localeKey;
    }

    public void addGuiElements(GuiElementContainer container) {
        GuiBC8<?> gui = container.gui;
        String localized = LocaleUtil.localize(localeKey);
        List<String> lines = StringUtilBC.splitIntoLines(localized);

        int y = 20;
        for (String line : lines) {
            GuiElementText elemText = new GuiElementText(gui, container, new GuiRectangle(0, y, 0, 0), line, 0, true);
            container.add(elemText);
            y += elemText.getHeight() + 5;
        }
    }
}
