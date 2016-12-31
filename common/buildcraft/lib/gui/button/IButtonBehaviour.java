package buildcraft.lib.gui.button;

public interface IButtonBehaviour {
    void mousePressed(GuiAbstractButton button);

    void mouseReleased(GuiAbstractButton button);

    public static final IButtonBehaviour DEFAULT = new IButtonBehaviour() {
        @Override
        public void mousePressed(GuiAbstractButton button) {
            button.active = true;
            button.notifyButtonStateChange();
        }

        @Override
        public void mouseReleased(GuiAbstractButton button) {
            button.active = false;
        }
    };

    public static final IButtonBehaviour TOGGLE = new IButtonBehaviour() {
        @Override
        public void mousePressed(GuiAbstractButton button) {
            if (button.active) {
                button.active = false;
                button.notifyButtonStateChange();
            } else {
                button.active = true;
                button.notifyButtonStateChange();
            }
        }

        @Override
        public void mouseReleased(GuiAbstractButton button) {}
    };

    public static Radio createAndSetRadioButtons(GuiAbstractButton... buttons) {
        Radio radio = new Radio(buttons);
        for (GuiAbstractButton button : buttons) {
            button.setBehaviour(radio);
        }
        return radio;
    }

    /** A radio button is a button linked to several other buttons, of which only 1 can be pressed at a time. */
    public static class Radio implements IButtonBehaviour {
        public final GuiAbstractButton[] buttons;

        public Radio(GuiAbstractButton... buttons) {
            this.buttons = buttons;
        }

        @Override
        public void mousePressed(GuiAbstractButton button) {
            for (GuiAbstractButton toDisable : buttons) {
                if (toDisable == button) {
                    if (!button.active) {
                        button.active = true;
                        button.notifyButtonStateChange();
                    }
                } else {
                    toDisable.active = false;
                }
            }
        }

        @Override
        public void mouseReleased(GuiAbstractButton button) {
            // NO-OP
        }
    }
}
