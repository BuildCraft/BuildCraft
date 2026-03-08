package buildcraft.lib.cache;

import net.minecraft.tileentity.TileEntity;

import java.util.function.Function;

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
