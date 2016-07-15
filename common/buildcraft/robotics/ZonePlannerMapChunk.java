package buildcraft.robotics;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

public class ZonePlannerMapChunk {
    public Map<BlockPos, Integer> data = new HashMap<>();

    public ZonePlannerMapChunk load(World world, int chunkX, int chunkZ) {
        data.clear();
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                BlockPos pos = new BlockPos(x, 0xFF, z);
                int color = 0;
                while(pos.getY() > 0 && color == 0) {
                    IBlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();
                    MapColor mapColor = block.getMapColor(state);
                    color = mapColor.colorValue;
                    pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
                }
                data.put(pos, color);
            }
        }
        return this;
    }
}
