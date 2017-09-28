package buildcraft.lib.gui;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiPosition;

public interface IContainingElement extends IInteractionElement {
    /** @return The backing list of the contained elements. Must be modifiable, and changes must be reflected by future
     *         calls. */
    List<IGuiElement> getChildElements();

    default IGuiPosition getChildElementPosition() {
        return this;
    }

    /** Called after {@link #getChildElements()} is added to, possibly last (so it might not be called after every
     * addition). */
    default void calculateSizes() {

    }

    @Override
    default void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement elem : getChildElements()) {
            elem.addToolTips(tooltips);
        }
    }

    @Override
    default void addHelpElements(List<HelpPosition> elements) {
        for (IGuiElement elem : getChildElements()) {
            elem.addHelpElements(elements);
        }
    }

    @Override
    default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        List<IGuiElement> list = new ArrayList<>();
        if (contains(x, y)) {
            list.add(this);
            for (IGuiElement elem : getChildElements()) {
                list.addAll(elem.getThisAndChildrenAt(x, y));
            }
        }
        return list;
    }

    @Override
    default void onMouseClicked(int button) {
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseClicked(button);
            }
        }
    }

    @Override
    default void onMouseReleased(int button) {
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseReleased(button);
            }
        }
    }

    @Override
    default void onMouseDragged(int button, long ticksSinceClick) {
        for (IGuiElement elem : getChildElements()) {
            if (elem instanceof IInteractionElement) {
                ((IInteractionElement) elem).onMouseDragged(button, ticksSinceClick);
            }
        }
    }
}
