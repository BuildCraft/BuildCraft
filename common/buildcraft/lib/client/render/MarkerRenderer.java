package buildcraft.lib.client.render;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerCache.SubCache;
import buildcraft.lib.marker.MarkerConnection;

public enum MarkerRenderer implements IDetachedRenderer {
    INSTANCE;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        for (MarkerCache<? extends SubCache<?>> cache : MarkerCache.CACHES) {
            renderCache(cache.getSubCache(player.worldObj));
        }
    }

    private static <C extends MarkerConnection<C>> void renderCache(SubCache<C> cache) {
        for (C connection : cache.getConnections()) {
            connection.renderInWorld();
        }
    }
}
