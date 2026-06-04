/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): zone planner map rendering (LWJGL + GL11) deferred
package buildcraft.robotics.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.GuiBC8;

import buildcraft.robotics.container.ContainerZonePlanner;

@Environment(EnvType.CLIENT)
public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {

    public GuiZonePlanner(ContainerZonePlanner container) {
        super(container);
    }
}
