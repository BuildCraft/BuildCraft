package buildcraft.core.tablet.manager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

import buildcraft.core.tablet.TabletClient;

public class TabletManagerClient {
	public static final TabletManagerClient INSTANCE = new TabletManagerClient();

	private static TabletClient currentTablet;
	private static TabletThread currentTabletThread;

	public TabletThread get() {
		if (currentTablet == null) {
			currentTablet = new TabletClient();
			currentTabletThread = new TabletThread(currentTablet);
			new Thread(currentTabletThread).start();
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
