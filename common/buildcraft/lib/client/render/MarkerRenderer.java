package buildcraft.lib.client.render;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;

public enum MarkerRenderer implements IDetachedRenderer {
    INSTANCE;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
            renderCache(cache.getSubCache(player.world));
        }
    }

    private static <C extends MarkerConnection<C>> void renderCache(MarkerSubCache<C> cache) {
        for (C connection : cache.getConnections()) {
            connection.renderInWorld();
        }
    }
}
