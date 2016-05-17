package buildcraft.lib.client.render;

import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.tile.MarkerCache;
import buildcraft.lib.tile.TileMarkerBase;

public class RenderMarkerHelper<T extends TileMarkerBase<T>> {
    private final MarkerCache<T> cache;

    public RenderMarkerHelper(MarkerCache<T> cache) {
        this.cache = cache;
    }

    public void iterateAll(World world, IMarkerRenderer<T> renderer) {
        Map<BlockPos, T> cache = this.cache.getCache(world);
        for (T rendering : cache.values()) {
            if (rendering == null) continue;
            renderer.render(rendering);
        }
    }

    @FunctionalInterface
    public interface IMarkerRenderer<T extends TileMarkerBase<T>> {
        void render(T marker);
    }
}
