package buildcraft.lib.cache;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;

public final class TileCacheRet {

    /** If this is null then that means we *know* the tile wasn't found, and so callers don't need to bother looking in
     * the world manually. */
    @Nullable
    public final WeakReference<TileEntity> ref;

    public TileCacheRet(WeakReference<TileEntity> ref) {
        this.ref = ref;
    }

    public TileCacheRet(TileEntity tile) {
        this.ref = tile == null ? null : new WeakReference<>(tile);
    }

    public TileEntity get() {
        return ref != null ? ref.get() : null;
    }
}
