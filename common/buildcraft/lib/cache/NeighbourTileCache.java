package buildcraft.lib.cache;

import buildcraft.lib.misc.ChunkUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.FaceDistance;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

/** An {@link ITileCache} that only caches the immediate neighbours of a {@link BlockEntity}. (Essentially caches
 * everything that {@link TileBC_Neptune#getNeighbourTile(Direction)} can return). */
public class NeighbourTileCache implements ITileCache {

    // TODO: Test the performance!

    private final BlockEntity tile;
    private BlockPos lastSeenTilePos;
    private final Map<Direction, WeakReference<BlockEntity>> cachedTiles = new EnumMap<>(Direction.class);

    public NeighbourTileCache(BlockEntity tile) {
        this.tile = tile;
    }

    @Override
    public void invalidate() {
        cachedTiles.clear();
    }

    @Override
    public TileCacheRet getTile(BlockPos pos) {
        if (!canUseCache()) {
            return null;
        }
        FaceDistance offset = PositionUtil.getDirectOffset(lastSeenTilePos, pos);
        if (offset == null || offset.distance != 1) {
            return null;
        }
        return getTile0(offset.direction);
    }

    private boolean canUseCache() {
        Level w = tile.getLevel();
        if (tile.isRemoved() || w == null) {
            return false;
        }
        BlockPos tPos = tile.getBlockPos();
        if (!tPos.equals(lastSeenTilePos)) {
            lastSeenTilePos = tPos.immutable();
            cachedTiles.clear();
        }
        if (!w.isLoaded(lastSeenTilePos)) {
            cachedTiles.clear();
            return false;
        }
        return true;
    }

    @Override
    public TileCacheRet getTile(Direction offset) {
        if (!canUseCache()) {
            return null;
        }
        return getTile0(offset);
    }

    private TileCacheRet getTile0(Direction offset) {
        WeakReference<BlockEntity> ref = cachedTiles.get(offset);
        if (ref != null) {
            BlockEntity oTile = ref.get();
            if (oTile == null || oTile.isRemoved()) {
                cachedTiles.remove(offset);
            } else {
                Level w = tile.getLevel();
                // Unfortunately tile.isInvalid is false even when it is unloaded
                if (w == null || !w.isLoaded(lastSeenTilePos.relative(offset))) {
                    cachedTiles.remove(offset);
                } else {
                    return new TileCacheRet(oTile);
                }
            }
        }
        BlockPos offsetPos = lastSeenTilePos.relative(offset);

        LevelChunk chunk;
        if (tile instanceof TileBC_Neptune) {
            chunk = ((TileBC_Neptune) tile).getChunk(offsetPos);
        } else {
            chunk = ChunkUtil.getChunk(tile.getLevel(), offsetPos, true);
        }
        BlockState state = chunk.getBlockState(offsetPos);
        if (!state.hasBlockEntity()) {
            // Optimisation: world.getTileEntity can be slow (as it potentially iterates through a long list)
            // so just check to make sure the target block might actually have a tile entity
            return new TileCacheRet(null);
        }

        BlockEntity offsetTile = tile.getLevel().getBlockEntity(offsetPos);
        if (offsetTile != null) {
            cachedTiles.put(offset, new WeakReference<>(offsetTile));
        }
        return new TileCacheRet(offsetTile);
    }
}
