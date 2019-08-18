package buildcraft.lib.cache;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import buildcraft.lib.misc.ChunkUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.FaceDistance;
import buildcraft.lib.tile.TileBC_Neptune;

/** An {@link ITileCache} that only caches the immediate neighbours of a {@link TileEntity}. (Essentially caches
 * everything that {@link TileBC_Neptune#getNeighbourTile(EnumFacing)} can return). */
public class NeighbourTileCache implements ITileCache {

    // TODO: Test the performance!

    private final TileEntity tile;
    private BlockPos lastSeenTilePos;
    private final BlockPos[] offsetPositions = new BlockPos[6];
    private final Map<EnumFacing, TileCacheRet> cachedTiles = new EnumMap<>(EnumFacing.class);

    public NeighbourTileCache(TileEntity tile) {
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
        World w = tile.getWorld();
        if (tile.isInvalid() || w == null) {
            return false;
        }
        BlockPos tPos = tile.getPos();
        if (!tPos.equals(lastSeenTilePos)) {
            lastSeenTilePos = tPos.toImmutable();
            cachedTiles.clear();
            for (EnumFacing face : EnumFacing.values()) {
                offsetPositions[face.ordinal()] = lastSeenTilePos.offset(face);
            }
        }
        if (!w.isBlockLoaded(lastSeenTilePos)) {
            cachedTiles.clear();
            return false;
        }
        return true;
    }

    @Override
    public TileCacheRet getTile(EnumFacing offset) {
        if (!canUseCache()) {
            return null;
        }
        return getTile0(offset);
    }

    private TileCacheRet getTile0(EnumFacing offset) {
        TileCacheRet ret = getTile0a(offset);
        if (ret != null) {
            return ret;
        }
        BlockPos offsetPos = offsetPositions[offset.ordinal()];
        IBlockState state = getTile0b(offsetPos);
        return getTile0c(offset, offsetPos, state);
    }

    private TileCacheRet getTile0a(EnumFacing offset) {
        TileCacheRet ref = cachedTiles.get(offset);
        if (ref != null) {
            if (ref.ref == null) {
                return ref;
            }
            TileEntity oTile = ref.get();
            if (oTile == null || oTile.isInvalid()) {
                cachedTiles.remove(offset);
            } else {
                World w = tile.getWorld();
                // Unfortunately tile.isInvalid is false even when it is unloaded
                if (w == null || !w.isBlockLoaded(offsetPositions[offset.ordinal()])) {
                    cachedTiles.remove(offset);
                } else {
                    return ref;
                }
            }
        }
        return null;
    }

    private IBlockState getTile0b(BlockPos offsetPos) {
        Chunk chunk;
        if (tile instanceof TileBC_Neptune) {
            chunk = ((TileBC_Neptune) tile).getChunk(offsetPos);
        } else {
            chunk = ChunkUtil.getChunk(tile.getWorld(), offsetPos, true);
        }
        IBlockState state = chunk.getBlockState(offsetPos);
        return state;
    }

    private TileCacheRet getTile0c(EnumFacing offset, BlockPos offsetPos, IBlockState state) {
        // Optimisation: world.getTileEntity can be slow (as it potentially iterates through a long list)
        // so just check to make sure the target block might actually have a tile entity
        final TileEntity offsetTile;
        if (state.getBlock().hasTileEntity(state)) {
            offsetTile = tile.getWorld().getTileEntity(offsetPos);
        } else {
            offsetTile = null;
        }
        TileCacheRet ref = new TileCacheRet(offsetTile);
        cachedTiles.put(offset, ref);
        return ref;
    }

}
