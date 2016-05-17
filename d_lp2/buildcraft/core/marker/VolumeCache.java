package buildcraft.core.marker;

import net.minecraft.world.World;

import buildcraft.core.marker.VolumeCache.PerWorldVolume;
import buildcraft.core.tile.TileMarkerVolume2;
import buildcraft.lib.marker.MarkerCache2;

public class VolumeCache extends MarkerCache2<PerWorldVolume> {
    public static final VolumeCache INSTANCE = new VolumeCache();

    private VolumeCache() {
        super("volume");
    }

    @Override
    protected PerWorldVolume createSubCache(World world) {
        return new PerWorldVolume(world);
    }

    public class PerWorldVolume extends MarkerCache2.PerWorld2<VolumeConnection, TileMarkerVolume2> {
        public PerWorldVolume(World world) {

        }
    }
}
