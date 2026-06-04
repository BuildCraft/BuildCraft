/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
// STUB(R.Chen): recipe book GUI integration + IRecipeShownListener deferred
package buildcraft.factory.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.gui.GuiBC8;

import buildcraft.factory.container.ContainerAutoCraftItems;

@Environment(EnvType.CLIENT)
public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> {

    public GuiAutoCraftItems(ContainerAutoCraftItems container) {
        super(container);
    }
}
