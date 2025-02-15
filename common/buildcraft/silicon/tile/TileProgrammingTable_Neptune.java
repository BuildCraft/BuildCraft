/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.container.ContainerProgrammingTable_Neptune;
import buildcraft.silicon.BCSiliconBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TileProgrammingTable_Neptune extends TileLaserTableBase {
    public TileProgrammingTable_Neptune(BlockPos pos, BlockState blockState) {
        super(BCSiliconBlocks.programmingTableTile.get(), pos, blockState);
    }

    @Override
    public long getTarget() {
        return 0;
    }

    // MenuProvider

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerProgrammingTable_Neptune(BCRoboticsMenuTypes.PROGRAMMING_TABLE, id, player, this);
    }

//    @Override
//    public boolean hasFastRenderer() {
//        return true;
//    }
}
