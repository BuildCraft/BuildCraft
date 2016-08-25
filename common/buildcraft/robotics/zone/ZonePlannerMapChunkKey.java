package buildcraft.robotics.zone;

import net.minecraft.util.math.ChunkPos;

import io.netty.buffer.ByteBuf;

public class ZonePlannerMapChunkKey {
    public static final int LEVEL_HEIGHT = 32;

    public final ChunkPos chunkPos;
    public final int dimensionalId;
    public final int level;

    public ZonePlannerMapChunkKey(ChunkPos chunkPos, int dimensionalId, int level) {
        this.chunkPos = chunkPos;
        this.dimensionalId = dimensionalId;
        this.level = level;
    }

    public ZonePlannerMapChunkKey(ByteBuf buf) {
        chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
        dimensionalId = buf.readInt();
        level = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkPos.chunkXPos);
        buf.writeInt(chunkPos.chunkZPos);
        buf.writeInt(dimensionalId);
        buf.writeInt(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZonePlannerMapChunkKey that = (ZonePlannerMapChunkKey) o;

        if (dimensionalId != that.dimensionalId) {
            return false;
        }
        if (level != that.level) {
            return false;
        }
        return chunkPos.equals(that.chunkPos);

    }

    @Override
    public int hashCode() {
        int result = chunkPos.hashCode();
        result = 31 * result + dimensionalId;
        result = 31 * result + level;
        return result;
    }
}
