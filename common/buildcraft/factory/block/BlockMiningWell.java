/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.block.IBlockWithTickableTE;
import net.minecraft.block.AbstractBlock;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockMiningWell extends BlockBCTile_Neptune<TileMiningWell> implements IBlockWithFacing, IBlockWithTickableTE<TileMiningWell> {
    public BlockMiningWell(String idBC, AbstractBlock.Properties props) {
        super(idBC, props);
    }

    @Nullable
    @Override
//    public TileBC_Neptune createTileEntity(World worldIn, IBlockState state)
    public TileMiningWell newBlockEntity(IBlockReader world) {
        return new TileMiningWell();
    }
}
