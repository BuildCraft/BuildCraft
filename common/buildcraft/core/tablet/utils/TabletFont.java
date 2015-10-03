package buildcraft.core.tablet.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import buildcraft.api.tablet.TabletBitmap;

public final class TabletFont {
	public final class Glyph {
		public byte[] glyphData;
		private final int loadOffset;
		private int width, height, xOffset, yOffset, deviceWidth;

		public Glyph(int offset) {
			this.loadOffset = offset;
		}

		public void load(byte[] data, int offsetBase) throws Exception {
			ByteArrayInputStream stream = new ByteArrayInputStream(data);
			stream.skip(loadOffset - offsetBase);
			width = readUnsignedShort(stream);
			height = readUnsignedShort(stream);
			xOffset = readShort(stream);
			yOffset = readShort(stream);
			deviceWidth = readShort(stream);

			glyphData = new byte[(width * height + 7) / 8];
			stream.read(glyphData);
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getDeviceWidth() {
			return deviceWidth;
		}

		public int getXOffset() {
			return xOffset;
		}

		public int getYOffset() {
			return yOffset;
		}

		public int draw(TabletBitmap bitmap, int x, int y, int intensity) {
			// TODO: Why do we have to do this?
			int yTop = y - (yOffset * 2) - height;
			for (int j = 0; j < height; j++) {
				for (int i = 0; i < width; i++) {
					int bPos = (i + (j * width)) >> 3;
					int bMask = 128 >> ((i + (j * width)) & 7);
					if ((glyphData[bPos] & bMask) != 0) {
						bitmap.set(x + xOffset + i, yTop + j, intensity);
					}
				}
			}
			return deviceWidth;
		}
	}

	private boolean isBold;
	private boolean isItalic;
	private int pointSize, maxW, maxH, ascent, descent;
	private TIntObjectMap<Glyph> glyphs = new TIntObjectHashMap<Glyph>();

	public TabletFont(File file) throws Exception {
		this(new FileInputStream(file));
	}

	public TabletFont(InputStream stream) throws Exception {
		int loaded = 0;

		while (stream.available() > 0) {
			String section = readString(stream, 4);
			int sectionLength = readInt(stream);
			loaded += 8;

			if ("FAMI".equals(section)) {
				readString(stream, sectionLength);
			} else if ("WEIG".equals(section)) {
				this.isBold = "bold".equals(readString(stream, sectionLength));
			} else if ("SLAN".equals(section)) {
				this.isItalic = "italic".equals(readString(stream, sectionLength));
			} else if ("PTSZ".equals(section)) {
				this.pointSize = readUnsignedShort(stream);
			} else if ("MAXW".equals(section)) {
				this.maxW = readUnsignedShort(stream);
			} else if ("MAXH".equals(section)) {
				this.maxH = readUnsignedShort(stream);
			} else if ("ASCE".equals(section)) {
				this.ascent = readUnsignedShort(stream);
			} else if ("DESC".equals(section)) {
				this.descent = readUnsignedShort(stream);
			} else if ("CHIX".equals(section)) {
				for (int i = 0; i < sectionLength; i += 9) {
					int codePoint = readInt(stream);
					stream.skip(1);
					int offset = readInt(stream);
					glyphs.put(codePoint, new Glyph(offset));
				}
			} else if ("DATA".equals(section)) {
				byte[] data = new byte[stream.available()];
				stream.read(data);

				for (Glyph g : glyphs.valueCollection()) {
					g.load(data, loaded);
				}
			} else {
				stream.skip(sectionLength);
			}
			loaded += sectionLength;
		}

		stream.close();
	}

	public boolean isBold() {
		return isBold;
	}

	public boolean isItalic() {
		return isItalic;
	}

	public int getPointSize() {
		return pointSize;
	}

	public int getWidth() {
		return maxW;
	}

	public int getHeight() {
		return maxH;
	}

	public int getAscent() {
		return ascent;
	}

	public int getDescent() {
		return descent;
	}

	public Glyph getGlyph(int codePoint) {
		return glyphs.get(codePoint);
	}


	public int getStringWidth(String s) {
		int width = 0;
		for (int i = 0; i < s.length(); i++) {
			width += getGlyph(s.codePointAt(i)).getDeviceWidth();
		}
		return width;
	}

	public int draw(TabletBitmap target, String s, int x, int y, int intensity) {
		int width = 0;
		for (int i = 0; i < s.length(); i++) {
			width += getGlyph(s.codePointAt(i)).draw(target, x + width, y + ascent, intensity);
		}
		return width;
	}

	private static int readUnsignedShort(InputStream stream) {
		try {
			int hi = stream.read();
			return hi << 8 | stream.read();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static int readShort(InputStream stream) {
		int t = readUnsignedShort(stream);
		if (t >= 0x8000) {
			return 0 - (t ^ 0xFFFF);
		} else {
			return t;
		}
	}

	private static int readInt(InputStream stream) {
		try {
			int i = stream.read();
			i = (i << 8) | stream.read();
			i = (i << 8) | stream.read();
			i = (i << 8) | stream.read();
			return i;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static String readString(InputStream stream, int length) {
		byte[] data = new byte[length];
		try {
			stream.read(data);
			return new String(data, "ASCII");
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void main(String[] args) {
		TabletBitmap bitmap = new TabletBitmap(244, 306);
		try {
			TabletFont font = new TabletFont(TabletFont.class.getClassLoader().getResourceAsStream("assets/buildcraftcore/tablet/test.pf2"));
			font.draw(bitmap, "Hello World!", 1, 1, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
