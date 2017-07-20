/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerProgrammingTable_Neptune extends ContainerBCTile<TileProgrammingTable_Neptune> {
    public ContainerProgrammingTable_Neptune(EntityPlayer player, TileProgrammingTable_Neptune tile) {
        super(player, tile);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.canInteractWith(playerIn);
    }
}
