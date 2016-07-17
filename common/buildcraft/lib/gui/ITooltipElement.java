package buildcraft.lib.gui;

import java.util.List;

import buildcraft.core.lib.gui.tooltips.ToolTip;

public interface ITooltipElement {
    /** Called to add tooltips to the list of existing tool tips. You MUST refresh the Tooltips if they need to be
     * refreshed. */
    default void addToolTips(List<ToolTip> tooltips) {}
}
