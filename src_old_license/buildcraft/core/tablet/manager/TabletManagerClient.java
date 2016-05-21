package buildcraft.core.tablet.manager;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import buildcraft.core.tablet.TabletClient;

public enum TabletManagerClient {
    INSTANCE;

    private static TabletClient currentTablet;
    private static TabletThread currentTabletThread;

    public TabletThread get() {
        if (currentTablet == null) {
            currentTablet = new TabletClient();
            currentTabletThread = new TabletThread(currentTablet);
            new Thread(currentTabletThread, "BuildCraft Tablet Manager").start();
        }
        return currentTabletThread;
    }

    public void onServerStopping() {
        if (currentTablet != null) {
            currentTablet = null;
            currentTabletThread.stop();
            currentTabletThread = null;
        }
    }

    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (currentTablet != null) {
            currentTablet = null;
            currentTabletThread.stop();
            currentTabletThread = null;
        }
    }
}
