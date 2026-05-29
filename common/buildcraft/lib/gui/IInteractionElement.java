/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// STUB(R.Chen): GUI render — Phase 5. org.lwjgl.input.Keyboard removed (LWJGL3 uses GLFW).
@Environment(EnvType.CLIENT)
public interface IInteractionElement extends IGuiElement {
    default void onMouseClicked(int button) {}
    default void onMouseDragged(int button, long ticksSinceClick) {}
    default void onMouseReleased(int button) {}
    default boolean onKeyPress(char typedChar, int keyCode) { return false; }
}
