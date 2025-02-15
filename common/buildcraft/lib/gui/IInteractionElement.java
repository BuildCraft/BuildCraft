package buildcraft.lib.gui;

import com.mojang.blaze3d.platform.InputConstants;

public interface IInteractionElement extends IGuiElement {

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
//    default void onMouseDragged(int button, long ticksSinceClick) {}
    default void onMouseDragged(int button) {
    }

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {
    }

    /** This is called EVEN IF the mouse is not inside your width and height!
     *
     * @return True if this element handled the press, false otherwise. If this returns true then the normal gui
     *         interaction events won't happen (for example if the player presses {@link InputConstants#KEY_ESCAPE escape} and
     *         this returns true then the gui won't be closed). It is <i>highly</i> recommended that you close something
     *         if {@link InputConstants#KEY_ESCAPE escape} is pressed. */
//    default boolean onKeyPress(char typedChar, int keyCode)
    default boolean onKeyPress(int typedChar, int keyCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char typedChar, int keyCode) {
        return false;
    }
}
