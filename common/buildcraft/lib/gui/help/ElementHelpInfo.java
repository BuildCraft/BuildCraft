package buildcraft.lib.gui.help;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.elem.GuiElementContainer;
import buildcraft.lib.gui.elem.GuiElementText;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;

/** Defines some information used when displaying help text about a specific {@link IGuiElement}. If you want to display
 * help at a particular position, but the target is not an {@link IGuiElement} then you should use
 * {@link DummyHelpElement}. */
public class ElementHelpInfo {
    public final String title;
    public final int colour;
    public final String[] localeKeys;

    public ElementHelpInfo(String title, int colour, String... localeKeys) {
        this.title = title;
        this.colour = colour;
        this.localeKeys = localeKeys;
    }

    @SideOnly(Side.CLIENT)
    public final HelpPosition target(IGuiArea target) {
        return new HelpPosition(this, target);
    }

    @SideOnly(Side.CLIENT)
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

    /** Stores an {@link ElementHelpInfo} information, as well as the target area which the help element relates to. */
    @SideOnly(Side.CLIENT)
    public static final class HelpPosition {
        public final ElementHelpInfo info;
        public final IGuiArea target;

        private HelpPosition(ElementHelpInfo info, IGuiArea target) {
            this.info = info;
            this.target = target;
        }
    }
}
