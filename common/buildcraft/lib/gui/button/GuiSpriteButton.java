/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.ISimpleDrawable;

public class GuiSpriteButton extends GuiAbstractButton {

    private final ISimpleDrawable drEnabled, drActive, drHovered, drActiveHovered, drDisabled;

    /** @param gui
     * @param buttonId
     * @param x
     * @param y
     * @param buttonStates The states. 0 should be the default (enabled, not active and not hovered), 1 should be
     *            active, [2 is hovered and 3 is active and hovered [4 is not enabled]] */
    public GuiSpriteButton(GuiBC8<?> gui, int buttonId, int x, int y, int width, int height, ISimpleDrawable... buttonStates) {
        super(gui, buttonId, x, y, "");
        switch (buttonStates.length) {
            case 0: {
                throw new IllegalArgumentException("Not enough states!");
            }
            case 1: {
                drEnabled = buttonStates[0];
                drHovered = drActive = drActiveHovered = drDisabled = drEnabled;
                break;
            }
            case 2: {
                drEnabled = drHovered = buttonStates[0];
                drDisabled = drActiveHovered = drActive = buttonStates[1];
                break;
            }
            case 3: {
                drEnabled = buttonStates[0];
                drActive = drActiveHovered = buttonStates[1];
                drDisabled = drHovered = buttonStates[2];
                break;
            }
            case 4: {
                drEnabled = buttonStates[0];
                drActive = buttonStates[1];
                drHovered = buttonStates[2];
                drActiveHovered = buttonStates[3];
                drDisabled = drEnabled;
                break;
            }
            case 5: {
                drEnabled = buttonStates[0];
                drActive = buttonStates[1];
                drHovered = buttonStates[2];
                drActiveHovered = buttonStates[3];
                drDisabled = buttonStates[4];
                break;
            }
            default: {
                throw new IllegalArgumentException("Too many button states! (" + buttonStates.length + ")");
            }
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
        if (!visible) {
            return;
        }

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        if (enabled) {
            if (active) {
                if (isMouseOver()) {
                    drActiveHovered.drawAt(x, y);
                } else {
                    drActive.drawAt(x, y);
                }
            } else if (isMouseOver()) {
                drHovered.drawAt(x, y);
            } else {
                drEnabled.drawAt(x, y);
            }
        } else {
            drDisabled.drawAt(x, y);
        }
    }

}
