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
    private static final RegistrationHelper HELPER = new RegistrationHelper();

    public static BlockFilteredBuffer filteredBuffer;
    public static BlockPipeHolder pipeHolder;

    public static void preInit() {
        filteredBuffer = HELPER.addBlockAndItem(new BlockFilteredBuffer(Material.IRON, "block.filtered_buffer"));
        pipeHolder = HELPER.addBlock(new BlockPipeHolder(Material.IRON, "block.pipe_holder"));

        HELPER.registerTile(TileFilteredBuffer.class, "tile.filtered_buffer");
        HELPER.registerTile(TilePipeHolder.class, "tile.pipe_holder");
    }
}
