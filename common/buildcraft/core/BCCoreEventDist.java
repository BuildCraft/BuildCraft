package buildcraft.core;

import buildcraft.core.marker.volume.MessageVolumeMarkers;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeMarkers;
import buildcraft.lib.BCMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public enum BCCoreEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if(!event.world.isRemote && event.world.getMinecraftServer() != null) {
            WorldSavedDataVolumeMarkers.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            BCMessageHandler.netWrapper.sendTo(
                    new MessageVolumeMarkers(WorldSavedDataVolumeMarkers.get(event.player.world).boxes),
                    (EntityPlayerMP) event.player
            );
            WorldSavedDataVolumeMarkers.get(((EntityPlayerMP) event.player).world).boxes.stream()
                    .filter(box -> box.isPausedEditingBy(event.player))
                    .forEach(VolumeBox::resumeEditing);
        }
    }
}
