package buildcraft.lib.cache;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public enum NoopTileCache implements ITileCache {
    INSTANCE;

    @Override
    public void invalidate() {
    }

    @Override
    public TileCacheRet getTile(BlockPos pos) {
        return null;
    }

    @Override
    public TileCacheRet getTile(Direction offset) {
        return null;
    }
}
