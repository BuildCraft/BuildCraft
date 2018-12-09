package buildcraft.lib.cache;

import java.util.function.Function;

import net.minecraft.tileentity.TileEntity;

public enum TileCacheType {
    NO_CACHE(tile -> NoopTileCache.INSTANCE),
    NEIGHBOUR_CACHE(NeighbourTileCache::new);

    private final Function<TileEntity, ITileCache> constructor;

    private TileCacheType(Function<TileEntity, ITileCache> constructor) {
        this.constructor = constructor;
    }

    public ITileCache create(TileEntity tile) {
        return constructor.apply(tile);
    }
}
