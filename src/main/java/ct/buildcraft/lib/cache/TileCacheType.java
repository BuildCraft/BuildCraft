package ct.buildcraft.lib.cache;

import java.util.function.Function;

import net.minecraft.world.level.block.entity.BlockEntity;

public enum TileCacheType {
    NO_CACHE(tile -> NoopTileCache.INSTANCE),
    NEIGHBOUR_CACHE(NeighbourTileCache::new);

    private final Function<BlockEntity, ITileCache> constructor;

    private TileCacheType(Function<BlockEntity, ITileCache> constructor) {
        this.constructor = constructor;
    }

    public ITileCache create(BlockEntity tile) {
        return constructor.apply(tile);
    }
}
