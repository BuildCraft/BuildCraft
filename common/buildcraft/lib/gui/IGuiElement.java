package buildcraft.lib.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.gui.tooltips.ToolTip;

@SideOnly(Side.CLIENT)
public interface IGuiElement extends IPositionedElement {
    default void drawBackground() {}

    default void drawForeground() {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseDragged(int button, long ticksSinceClick) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {}

    /** Helper method for creating a tooltip to be displayed as a gui element. */
    default ToolTip getToolTip() {
        return null;
    }
}
