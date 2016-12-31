package buildcraft.transport;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.wire.WorldSavedDataWireSystems;

public enum BCTransportEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote && event.world.getMinecraftServer() != null) {
            WorldSavedDataWireSystems.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent event) {
        WorldSavedDataWireSystems.get(event.getPlayer().world).changedPlayers.add(event.getPlayer());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Post event) {
        PipeWireRenderer.clearWireCache();
    }
}
