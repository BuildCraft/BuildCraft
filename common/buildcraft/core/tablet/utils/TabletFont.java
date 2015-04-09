package buildcraft.core.tablet.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class TabletFont {
	private String family;
	private boolean isBold;
	private boolean isItalic;
	private int pointSize, maxW, maxH, ascent, descent;

	public TabletFont(File file) throws Exception {
		this(new FileInputStream(file));
	}

	public TabletFont(InputStream stream) throws Exception {
		while (stream.available() > 0) {
			String section = readString(stream, 4);
			int sectionLength = readInt(stream);
			if ("FAMI".equals(section)) {
				this.family = readString(stream, sectionLength);
			} else if ("WEIG".equals(section)) {
				this.isBold = readString(stream, sectionLength).equals("bold");
			} else if ("SLAN".equals(section)) {
				this.isItalic = readString(stream, sectionLength).equals("italic");
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
			}
			// TODO: character index/data
		}
	}

	private static int readUnsignedShort(InputStream stream) {
		byte[] data = new byte[2];
		try {
			stream.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ((int) data[0] & 0xFF) << 8 | ((int) data[1] & 0xFF);
	}

	private static int readShort(InputStream stream) {
		int t = readUnsignedShort(stream);
		if (t >= 0x8000) {
			return 0x7FFF - t;
		} else {
			return t;
		}
	}

	private static int readInt(InputStream stream) {
		byte[] data = new byte[4];
		try {
			stream.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ((int) data[0] & 0xFF) << 24 | ((int) data[1] & 0xFF) << 16
				| ((int) data[2] & 0xFF) << 8 | ((int) data[3] & 0xFF);
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
}
