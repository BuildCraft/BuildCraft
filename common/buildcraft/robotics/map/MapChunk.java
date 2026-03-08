package buildcraft.robotics.map;

import buildcraft.api.core.BCLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;

public class MapChunk {
    private static final int VERSION = 1;

    private int x, z;
    private byte[] data;

    public MapChunk(int x, int z) {
        this.x = x;
        this.z = z;
        data = new byte[256];
    }

    public MapChunk(CompoundNBT compound) {
        readFromNBT(compound);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getColor(int x, int z) {
        return (int) data[((z & 15) << 4) | (x & 15)];
    }

    public void update(Chunk chunk) {
        for (int bz = 0; bz < 16; bz++) {
            for (int bx = 0; bx < 16; bx++) {
                int y = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, bx, bz);
                int color = MaterialColor.NONE.id;

                if (y < 0) {
                    y = 255;
                }

                Block b;
                BlockState state;

                while (y >= 0) {
                    BlockPos pos = new BlockPos(bx, y, bz);
                    state = chunk.getBlockState(pos);
                    b = state.getBlock();

                    // color = b.getMapColor(state) != null ? b.getMapColor(state).colorIndex : MapColor.airColor.colorIndex;
                    MaterialColor colour = state.getMapColor(chunk.getLevel(), pos);
                    color = colour != null ? colour.id : MaterialColor.NONE.id;
                    if (color != MaterialColor.NONE.id) {
                        break;
                    }
                    y--;
                }

                data[(bz << 4) | bx] = (byte) color;
            }
        }
    }

    public void readFromNBT(CompoundNBT compound) {
        int version = compound.getShort("version");
        if (version > MapChunk.VERSION) {
            BCLog.logger.error("Unsupported MapChunk version: " + version);
            return;
        }
        x = compound.getInt("x");
        z = compound.getInt("z");
        data = compound.getByteArray("data");
        if (data.length != 256) {
            BCLog.logger.error("Invalid MapChunk data length: " + data.length);
            data = new byte[256];
        }
    }

    public void writeToNBT(CompoundNBT compound) {
        compound.putShort("version", (short) VERSION);
        compound.putInt("x", x);
        compound.putInt("z", z);
        compound.putByteArray("data", data);
    }

    @Override
    public int hashCode() {
        return 31 * x + z;
    }
}
