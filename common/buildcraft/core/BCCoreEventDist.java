package buildcraft.core;

import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public enum BCCoreEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote && event.world.getMinecraftServer() != null) {
            WorldSavedDataVolumeBoxes.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayed(() ->
                MessageManager.sendTo(
                    new MessageVolumeBoxes(WorldSavedDataVolumeBoxes.get(event.player.world).boxes),
                    (EntityPlayerMP) event.player
                )
            );
            WorldSavedDataVolumeBoxes.get(((EntityPlayerMP) event.player).world).boxes.stream()
                .filter(box -> box.isPausedEditingBy(event.player))
                .forEach(VolumeBox::resumeEditing);
        }
    }
}
