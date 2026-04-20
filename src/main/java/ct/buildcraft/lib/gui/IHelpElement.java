package ct.buildcraft.lib.gui;

import java.util.List;

import ct.buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;

@FunctionalInterface
public interface IHelpElement {
    void addHelpElements(List<HelpPosition> elements);
}
