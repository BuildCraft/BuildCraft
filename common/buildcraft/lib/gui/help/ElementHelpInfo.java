package buildcraft.lib.gui.help;

import java.util.List;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.elem.GuiElementContainer;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;

public class ElementHelpInfo {
    public final String title;
    public final int colour;
    public final String[] localeKeys;

    public ElementHelpInfo(String title, int colour, String... localeKeys) {
        this.title = title;
        this.colour = colour;
        this.localeKeys = localeKeys;
    }

    public final HelpPosition target(IGuiArea target) {
        return new HelpPosition(this, target);
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
                GuiElementText elemText = new GuiElementText(gui, container.offset(0, y), line, 0);
                container.add(elemText);
                y += elemText.getHeight() + 5;
            }
        }
    }

    public static final class HelpPosition {
        public final ElementHelpInfo info;
        public final IGuiArea target;

        private HelpPosition(ElementHelpInfo info, IGuiArea target) {
            this.info = info;
            this.target = target;
        }
    }
}
