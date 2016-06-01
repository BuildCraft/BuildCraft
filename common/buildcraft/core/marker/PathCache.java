package buildcraft.core.marker;

import net.minecraft.world.World;

import buildcraft.lib.marker.MarkerCache;

public class PathCache extends MarkerCache<PathSubCache> {
    public static final PathCache INSTANCE = new PathCache();

    public PathCache() {
        super("path");
    }

    @Override
    protected PathSubCache createSubCache(World world) {
        return new PathSubCache(world);
    }
}
