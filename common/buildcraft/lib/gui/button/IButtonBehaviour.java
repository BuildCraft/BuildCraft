package buildcraft.lib.gui.button;

public interface IButtonBehaviour {
    void mousePressed(GuiAbstractButton button, int bkey);

    void mouseReleased(GuiAbstractButton button, int bkey);

    public static final IButtonBehaviour DEFAULT = new IButtonBehaviour() {
        @Override
        public void mousePressed(GuiAbstractButton button, int bkey) {
            button.active = true;
        }

        @Override
        public void mouseReleased(GuiAbstractButton button, int bkey) {
            button.active = false;
            if (button.contains(button.gui.mouse)) {
                button.notifyButtonClicked(bkey);
            }
        }
    };

    public static final IButtonBehaviour TOGGLE = new IButtonBehaviour() {
        @Override
        public void mousePressed(GuiAbstractButton button, int bkey) {
            button.active = !button.active;
            button.notifyButtonClicked(bkey);
        }

        @Override
        public void mouseReleased(GuiAbstractButton button, int bkey) {}
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
        public void mousePressed(GuiAbstractButton button, int bkey) {
            for (GuiAbstractButton toDisable : buttons) {
                if (toDisable == button) {
                    if (!button.active) {
                        button.active = true;
                    }
                } else {
                    toDisable.active = false;
                }
            }
        }

        @Override
        public void mouseReleased(GuiAbstractButton button, int bkey) {
            if (button.contains(button.gui.mouse)) {
                button.notifyButtonClicked(bkey);
            }
            // NO-OP
        }
    }
}
