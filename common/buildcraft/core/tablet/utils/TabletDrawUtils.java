package buildcraft.core.tablet.utils;

import buildcraft.api.tablet.TabletBitmap;

public final class TabletDrawUtils {
	private TabletDrawUtils() {

	}

	public static void drawRect(TabletBitmap bitmap, int x, int y, int w, int h, int shade) {
		int x2 = x + w - 1;
		int y2 = y + h - 1;
		for (int i = 0; i < w; i++) {
			bitmap.set(x + i, y, shade);
			bitmap.set(x + i, y2, shade);
		}
		for (int i = 1; i < (h - 1); i++) {
			bitmap.set(x, y + i, shade);
			bitmap.set(x2, y + i, shade);
		}
	}

	public static void drawFilledRect(TabletBitmap bitmap, int x, int y, int w, int h, int shade) {
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				bitmap.set(x + i, y + j, bitmap);
			}
		}
	}
}
