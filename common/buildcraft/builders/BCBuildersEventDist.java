package buildcraft.builders;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import buildcraft.builders.bpt.player.PlayerBptBuilderManager;

public enum BCBuildersEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        PlayerBptBuilderManager.INSTANCE.onPlayerTick(event);
    }
}
