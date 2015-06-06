package buildcraft.core.tablet.manager;

import java.util.Date;

import buildcraft.core.tablet.TabletBase;

public class TabletThread implements Runnable {
	private final TabletBase tablet;

	private long begunTickDate;
	private long lastTickReceivedDate;
	private float ticksLeft = 0.0F;
	private boolean isRunning = false;

	public TabletThread(TabletBase tablet) {
		this.tablet = tablet;
		lastTickReceivedDate = begunTickDate = (new Date()).getTime();
	}

	public TabletBase getTablet() {
		return tablet;
	}

	@Override
	public void run() {
		isRunning = true;
		while (isRunning) {
			if (ticksLeft > 0.0F) {
				begunTickDate = (new Date()).getTime();
				tablet.tick(ticksLeft);
				float timeElapsed = (float) (lastTickReceivedDate - begunTickDate) / 1000.0F;
				if (timeElapsed > 0) {
					ticksLeft -= timeElapsed;
				}
			} else {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
				}
			}
		}
	}

	public void stop() {
		isRunning = false;
	}

	public void tick(float time) {
		ticksLeft += time;
		lastTickReceivedDate = (new Date()).getTime();
	}
}
