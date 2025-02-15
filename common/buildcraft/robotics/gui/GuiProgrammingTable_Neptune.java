/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.robotics.container.ContainerProgrammingTable_Neptune;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiProgrammingTable_Neptune extends GuiBC8<ContainerProgrammingTable_Neptune> {

    public GuiProgrammingTable_Neptune(ContainerProgrammingTable_Neptune container, Inventory inventory, Component component) {
        super(container, inventory, component);
    }
}
