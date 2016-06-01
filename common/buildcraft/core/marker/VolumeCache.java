package buildcraft.core.marker;

import net.minecraft.world.World;

import buildcraft.lib.marker.MarkerCache;

public class VolumeCache extends MarkerCache<VolumeSubCache> {
    public static final VolumeCache INSTANCE = new VolumeCache();

    private VolumeCache() {
        super("volume");
    }

    @Override
    protected VolumeSubCache createSubCache(World world) {
        return new VolumeSubCache(world);
    }
}
