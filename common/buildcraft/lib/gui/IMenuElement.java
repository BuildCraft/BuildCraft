package buildcraft.lib.gui;

/** An {@link IInteractionElement} that should be displayed above everything - except for tooltips. */
public interface IMenuElement extends IInteractionElement {
    /** @return True if other elements should be skipped when calculating tooltips, and a few other things. */
    default boolean shouldFullyOverride() {
        return true;
    }
}
