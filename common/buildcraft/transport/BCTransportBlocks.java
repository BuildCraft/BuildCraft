/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.block.material.Material;

import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.transport.block.BlockFilteredBuffer;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;

public class BCTransportBlocks {
    public static BlockFilteredBuffer filteredBuffer;
    public static BlockPipeHolder pipeHolder;

    public static void preInit() {
        filteredBuffer = RegistrationHelper.addBlockAndItem(new BlockFilteredBuffer(Material.IRON, "block.filtered_buffer"));
        pipeHolder = RegistrationHelper.addBlock(new BlockPipeHolder(Material.IRON, "block.pipe_holder"));

        RegistrationHelper.registerTile(TileFilteredBuffer.class, "tile.filtered_buffer");
        RegistrationHelper.registerTile(TilePipeHolder.class, "tile.pipe_holder");
    }
}
