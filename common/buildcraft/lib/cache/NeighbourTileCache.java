package buildcraft.lib.cache;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** An {@link ITileCache} that only caches the immediate neighbours of a {@link BlockEntity}. */
// STUB(R.Chen): the real neighbour-caching logic depends on the unmigrated PositionUtil / FaceDistance /
// VecUtil chain (and Forge's BlockState#hasTileEntity). Caching is a pure optimisation: returning null
// from getTile makes TileBC_Neptune fall back to a direct world lookup, so behaviour is preserved. The
// full cache is restored once PositionUtil + FaceDistance are migrated.
public class NeighbourTileCache implements ITileCache {

    @SuppressWarnings("unused")
    private final BlockEntity tile;

    public NeighbourTileCache(BlockEntity tile) {
        this.tile = tile;
    }

    @Override
    public void invalidate() {}

    @Override
    public TileCacheRet getTile(BlockPos pos) {
        return null;
    }

    @Override
    public TileCacheRet getTile(Direction offset) {
        return null;
    }
}
