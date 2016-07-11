package buildcraft.transport;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.block.BlockFilteredBuffer;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.block.material.Material;

public class BCTransportBlocks {
    public static BlockFilteredBuffer filteredBuffer;

    public static void preInit() {
        filteredBuffer = BlockBCBase_Neptune.register(new BlockFilteredBuffer(Material.ROCK, "block.filtered_buffer"));

        TileBC_Neptune.registerTile(TileFilteredBuffer.class, "tile.filtered_buffer");
    }
}
