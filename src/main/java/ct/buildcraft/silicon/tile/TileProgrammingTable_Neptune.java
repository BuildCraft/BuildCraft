/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import ct.buildcraft.silicon.BCSiliconBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileProgrammingTable_Neptune extends TileLaserTableBase {
	
    public TileProgrammingTable_Neptune(BlockPos pos, BlockState state) {
		super(BCSiliconBlocks.PROGRAMMING_TABLE_TILE.get(), pos, state);
	}

	@Override
    public long getTarget() {
        return 0;
    }

 /*   @Override
    public boolean hasFastRenderer() {
        return true;
    }*/
}
