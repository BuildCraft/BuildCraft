package buildcraft.robotics.zone;

import java.util.Arrays;

import net.minecraft.util.math.ChunkPos;

import io.netty.buffer.ByteBuf;

public class ZonePlannerMapChunkKey {
    public static final int LEVEL_HEIGHT = 32;

    public final ChunkPos chunkPos;
    public final int dimensionalId;
    public final int level;
    private final int hash;

    public ZonePlannerMapChunkKey(ChunkPos chunkPos, int dimensionalId, int level) {
        this.chunkPos = chunkPos;
        this.dimensionalId = dimensionalId;
        this.level = level;
        hash = Arrays.hashCode(new int[] { chunkPos.chunkXPos, chunkPos.chunkZPos, dimensionalId, level });
    }

    public ZonePlannerMapChunkKey(ByteBuf buf) {
        chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
        dimensionalId = buf.readInt();
        level = buf.readInt();
        hash = Arrays.hashCode(new int[] { chunkPos.chunkXPos, chunkPos.chunkZPos, dimensionalId, level });
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkPos.chunkXPos);
        buf.writeInt(chunkPos.chunkZPos);
        buf.writeInt(dimensionalId);
        buf.writeInt(level);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        ZonePlannerMapChunkKey other = (ZonePlannerMapChunkKey) o;
        if (dimensionalId != other.dimensionalId) return false;
        if (level != other.level) return false;
        return chunkPos.chunkXPos == other.chunkPos.chunkXPos && chunkPos.chunkZPos == other.chunkPos.chunkZPos;

    }

    @Override
    public int hashCode() {
        return hash;
    }
}
