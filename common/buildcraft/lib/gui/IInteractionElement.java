package buildcraft.lib.gui;

public interface IInteractionElement extends IGuiElement {

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseClicked(int button) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseDragged(int button, long ticksSinceClick) {}

    /** This is called EVEN IF the mouse is not inside your width and height! */
    default void onMouseReleased(int button) {}

}
