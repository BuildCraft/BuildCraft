/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.chunkload;

import net.minecraft.tileentity.TileEntity;

public class ChunkLoaderManager {
    /** This should be called in {@link TileEntity#validate()}, if a tile entity might be able to load. A check is
     * automatically performed to see if the config allows it, and if it it is set to STRICT then */
    public static void loadChunksForTile(IChunkLoadingTile tile) {
        if (tile == null) {
            throw new NullPointerException("tile");
        }
        if (!(tile instanceof TileEntity)) {
            throw new IllegalArgumentException("Can only chunkload tile entities! " + tile.getClass());
        }
    }
}
