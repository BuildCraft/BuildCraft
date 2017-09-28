package buildcraft.lib.gui;

import java.util.List;

import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;

@FunctionalInterface
public interface IHelpElement {
    void addHelpElements(List<HelpPosition> elements);
}
