package buildcraft.lib.chunkload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.BCLibConfig.ChunkLoaderLevel;

/** This should be implemented by {@link TileEntity}'s that wish to be chunkloaded by buildcraft lib. Note that tiles
 * should add themselves to the chunkloading list in {@link ChunkLoaderManager#loadChunksForTile(IChunkLoadingTile)} */
public interface IChunkLoadingTile {
    /** @return The chunkloading type, or null if this tile doesn't want to be chunkloaded. */
    @Nullable
    default LoadType getLoadType() {
        return LoadType.SOFT;
    }

    /** Gets a list of all the ADDITIONAL chunks to load.
     * 
     * The default implementation returns neighbouring chunks if this block is on a chunk boundary.
     * 
     * @return A collection of all the additional chunks to load, not including the {@link ChunkPos} that this tile is
     *         contained within. If the return value is null then only the chunk containing this block will be
     *         chunkloaded. */
    @Nullable
    default Collection<ChunkPos> getChunksToLoad() {
        BlockPos pos = ((TileEntity) this).getPos();
        ChunkPos thisPos = new ChunkPos(pos);
        List<ChunkPos> list = new ArrayList<>(4);
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            ChunkPos potential = new ChunkPos(pos.offset(face));
            if (!potential.equals(thisPos)) {
                list.add(potential);
            }
        }
        return list;
    }

    enum LoadType {
        /** Softly attempt to chunkload this. If the value of {@link BCLibConfig#chunkLoadingType} is equal to
         * {@link ChunkLoaderLevel#STRICT_TILES} or {@link ChunkLoaderLevel#NONE} then it won't be loaded. */
        SOFT,
        /** If the value of {@link BCLibConfig#chunkLoadingType} is equal to {@link ChunkLoaderLevel#NONE} then it won't
         * be loaded. Generally this should only be enabled for machines that are designed to operate far from a players
         * territory, like a quarry or a pump. */
        HARD
    }
}
