/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.lib.compat.MaterialBC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.tile.TilePump;

public class BlockPump extends BlockBCTile_Neptune {
    public BlockPump(AbstractBlock.Settings material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World worldIn, BlockState state) {
        return new TilePump();
    }
}
