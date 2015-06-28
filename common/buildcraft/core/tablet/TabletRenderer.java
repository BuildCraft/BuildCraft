package buildcraft.core.tablet;

import buildcraft.api.tablet.TabletBitmap;
import buildcraft.api.tablet.TabletTicker;

class TabletRenderer {
	private TabletBitmap currDisplay, newDisplay;
	private final TabletTicker refreshRate = new TabletTicker(0.035F);
	private boolean changed = false;
	private boolean isTicking = false;
	private int tickLocation = 7;

	public TabletRenderer(TabletBitmap display) {
		this.currDisplay = display;
	}

	public TabletBitmap get() {
		return currDisplay;
	}

	public boolean shouldChange() {
		boolean oldChanged = changed;
		changed = false;
		return oldChanged;
	}

	public void update(TabletBitmap display) {
		synchronized (refreshRate) {
			newDisplay = display;
			isTicking = true;
			tickLocation = 7;
			refreshRate.reset();
		}
	}

	public boolean tick(float tick) {
		if (isTicking) {
			synchronized (refreshRate) {
				refreshRate.add(tick);
				changed = false;
				for (int times = 0; times < refreshRate.getTicks(); times++) {
					for (int j = 0; j < currDisplay.height; j++) {
						for (int i = 0; i < currDisplay.width; i++) {
							int oldI = currDisplay.get(i, j);
							int newI = newDisplay.get(i, j);
							if (Math.abs(oldI - newI) == tickLocation) {
								if (oldI < newI) {
									changed = true;
									currDisplay.set(i, j, oldI + 1);
								} else if (oldI > newI) {
									changed = true;
									currDisplay.set(i, j, oldI - 1);
								}
							}
						}
					}

					tickLocation--;

					if (!changed || tickLocation == 0) {
						isTicking = false;
						break;
					}
				}

				refreshRate.tick();
			}

			return true;
		} else {
			return false;
		}
	}
}
