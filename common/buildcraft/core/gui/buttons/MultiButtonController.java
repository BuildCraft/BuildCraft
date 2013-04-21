package buildcraft.core.gui.buttons;

/**
 * T should be an Enum of button states
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class MultiButtonController<T extends IMultiButtonState> {

    private int currentState;
    private final T[] validStates;

    private MultiButtonController(int startState, T... validStates) {
        this.currentState = startState;
        this.validStates = validStates;
    }

    public static <T extends IMultiButtonState> MultiButtonController getController(int startState, T... validStates) {
        return new MultiButtonController<T>(startState, validStates);
    }

    public MultiButtonController copy() {
        return new MultiButtonController(currentState, validStates.clone());
    }

    public T[] getValidStates() {
        return validStates;
    }

    public int incrementState() {
        int newState = currentState + 1;
        if (newState >= validStates.length) {
            newState = 0;
        }
        currentState = newState;
        return currentState;
    }

    public void setCurrentState(int state) {
        currentState = state;
    }

    public int getCurrentState() {
        return currentState;
    }

    public T getButtonState() {
        return validStates[currentState];
    }
}
