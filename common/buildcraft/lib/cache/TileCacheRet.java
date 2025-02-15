package buildcraft.lib.cache;

import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public final class TileCacheRet {

    /** If this is null then that means we *know* the tile wasn't found, and so callers don't need to bother looking in
     * the world manually. */
    @Nullable
    public final BlockEntity tile;

    public TileCacheRet(BlockEntity tile) {
        this.tile = tile;
    }
}
