package buildcraft.robotics.zone;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ZonePlannerMapChunk {
    public Map<BlockPos, Integer> data = new HashMap<>();

    public ZonePlannerMapChunk(World world, ZonePlannerMapChunkKey key) {
        Chunk chunk = world.getChunkFromChunkCoords(key.chunkPos.chunkXPos, key.chunkPos.chunkZPos);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = new BlockPos(x, key.level * ZonePlannerMapChunkKey.LEVEL_HEIGHT, z);
                int color = 0;
                while (pos.getY() > 0 && color == 0) {
                    IBlockState state = chunk.getBlockState(pos);
                    MapColor mapColor = state.getMapColor();
                    color = mapColor.colorValue;
                    pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
                }
                data.put(pos, color);
            }
        }
    }

    public ZonePlannerMapChunk(PacketBuffer buffer) {
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            int colour = buffer.readInt();
            data.put(pos, Integer.valueOf(colour));
        }
    }

    public void write(PacketBuffer buffer) {
        buffer.writeInt(data.size());
        for (Entry<BlockPos, Integer> entry : data.entrySet()) {
            buffer.writeBlockPos(entry.getKey());
            buffer.writeInt(entry.getValue().intValue());
        }
    }
}
