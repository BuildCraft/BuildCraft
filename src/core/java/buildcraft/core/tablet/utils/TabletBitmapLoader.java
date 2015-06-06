package buildcraft.core.tablet.utils;

import java.io.InputStream;

import buildcraft.api.tablet.TabletBitmap;

public final class TabletBitmapLoader {
	private TabletBitmapLoader() {

	}

	/**
	 * This function takes a 16-bit grayscale RAW file (GIMP can output these)
	 */
	public static TabletBitmap createFromGray(InputStream stream, int width, int height) {
		try {
			byte[] data = new byte[stream.available()];
			stream.read(data);
			stream.close();

			TabletBitmap bitmap = new TabletBitmap(width, height);
			for (int i = 0; i < width * height; i++) {
				bitmap.set(i % width, i / width, ~((int) data[i * 2] >>> 5) & 7);
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
