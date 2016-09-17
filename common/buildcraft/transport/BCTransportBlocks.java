package buildcraft.transport;

import net.minecraft.block.material.Material;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.block.BlockFilteredBuffer;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;

public class BCTransportBlocks {
    public static BlockFilteredBuffer filteredBuffer;
    public static BlockPipeHolder pipeHolder;

    public static void preInit() {
        filteredBuffer = BlockBCBase_Neptune.register(new BlockFilteredBuffer(Material.ROCK, "block.filtered_buffer"));
        pipeHolder = BlockBCBase_Neptune.register(new BlockPipeHolder(Material.IRON, "block.pipe_holder"), null);

        TileBC_Neptune.registerTile(TileFilteredBuffer.class, "tile.filtered_buffer");
        TileBC_Neptune.registerTile(TilePipeHolder.class, "tile.pipe_holder");
    }
}
