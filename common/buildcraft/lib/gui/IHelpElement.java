package buildcraft.lib.gui;

import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;

import java.util.List;

@FunctionalInterface
public interface IHelpElement {
    void addHelpElements(List<HelpPosition> elements);
}
