package buildcraft.transport;

import buildcraft.transport.client.render.RenderPipeHolder;
import buildcraft.transport.wire.WorldSavedDataWireSystems;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public enum BCTransportEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if(!event.world.isRemote && event.world.getMinecraftServer() != null) {
            WorldSavedDataWireSystems.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent event) {
        WorldSavedDataWireSystems.get(event.getPlayer().worldObj).changedPlayers.add(event.getPlayer());
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Post event) {
        RenderPipeHolder.wiresRenderingCache.clear();
    }
}
