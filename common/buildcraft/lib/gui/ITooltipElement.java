package buildcraft.lib.gui;

import java.util.List;

import buildcraft.lib.gui.elem.ToolTip;

/** Defines some sort of element that should be queried to get tooltips that should be shown. */
public interface ITooltipElement {
    /** Called to add tooltips to the list of existing tool tips. You MUST refresh the Tooltips if they need to be
     * refreshed. */
    default void addToolTips(List<ToolTip> tooltips) {}
}
