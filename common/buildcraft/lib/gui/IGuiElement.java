package buildcraft.lib.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraft.lib.gui.pos.IGuiArea;

/** Defines an element that can be interacted with, that exists inside of a rectangle. */
@SideOnly(Side.CLIENT)
public interface IGuiElement extends IGuiArea, ITooltipElement {
    default void drawBackground(float partialTicks) {}

    default void drawForeground(float partialTicks) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseDragged(int button, long ticksSinceClick) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {}

    /** @return The {@link HelpPosition} pair, or null if this element shouldn't display help right now. */
    default HelpPosition getHelpInfo() {
        return null;
    }
}
