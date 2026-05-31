/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.button;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IButtonBehaviour {
    void mousePressed(GuiAbstractButton button, int bkey);
    void mouseReleased(GuiAbstractButton button, int bkey);

    IButtonBehaviour DEFAULT = new IButtonBehaviour() {
        @Override
        public void mousePressed(GuiAbstractButton button, int bkey) {
            button.active = true;
        }

        @Override
        public void mouseReleased(GuiAbstractButton button, int bkey) {
            if (button.active) {
                button.active = false;
                if (button.contains(button.gui.mouse)) {
                    button.notifyButtonClicked(bkey);
                }
            }
        }
    };

    IButtonBehaviour TOGGLE = new IButtonBehaviour() {
        @Override
        public void mousePressed(GuiAbstractButton button, int bkey) {
            button.active = !button.active;
            button.notifyButtonClicked(bkey);
        }

        @Override
        public void mouseReleased(GuiAbstractButton button, int bkey) {}
    };

    static Radio createAndSetRadioButtons(GuiAbstractButton... buttons) {
        Radio radio = new Radio(buttons);
        for (GuiAbstractButton button : buttons) {
            button.setBehaviour(radio);
        }
        return radio;
    }

    class Radio implements IButtonBehaviour {
        public final GuiAbstractButton[] buttons;

        public Radio(GuiAbstractButton... buttons) {
            this.buttons = buttons;
        }

        @Override
        public void mousePressed(GuiAbstractButton button, int bkey) {
            for (GuiAbstractButton toDisable : buttons) {
                if (toDisable == button) {
                    if (!button.active) button.active = true;
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
        }
    }
}
