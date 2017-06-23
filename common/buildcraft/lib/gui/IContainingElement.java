package buildcraft.lib.gui;

import java.util.List;

import buildcraft.lib.gui.elem.ToolTip;

public interface IContainingElement extends IGuiElement {
    /** @return The backing list of the contained elements. Must be modifiable, and changes must be reflected by future
     *         calls. */
    List<IGuiElement> getChildElements();

    @Override
    default void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement elem : getChildElements()) {
            elem.addToolTips(tooltips);
        }
    }
}
