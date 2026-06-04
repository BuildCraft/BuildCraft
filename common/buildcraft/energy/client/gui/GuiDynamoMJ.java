/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): GuiDynamoMJ deferred
package buildcraft.energy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.GuiBC8;

import buildcraft.energy.container.ContainerDynamoMJ;

@Environment(EnvType.CLIENT)
public class GuiDynamoMJ extends GuiBC8<ContainerDynamoMJ> {

    public GuiDynamoMJ(ContainerDynamoMJ container) {
        super(container);
    }
}
