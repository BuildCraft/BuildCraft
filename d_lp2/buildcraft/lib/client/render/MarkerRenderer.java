package buildcraft.lib.client.render;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.marker.MarkerCache2;
import buildcraft.lib.marker.MarkerCache2.SubCache2;
import buildcraft.lib.marker.MarkerConnection2;

public enum MarkerRenderer implements IDetachedRenderer {
    INSTANCE;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        for (MarkerCache2<? extends SubCache2<?>> cache : MarkerCache2.CACHES) {
            renderCache(cache.getSubCache(player.worldObj));
        }
    }

    private static <C extends MarkerConnection2<C>> void renderCache(SubCache2<C> cache) {
        for (C connection : cache.getConnections()) {
            connection.renderInWorld();
        }
    }
}
