package buildcraft.core.tablet.utils;

import java.util.ArrayList;
import java.util.List;

public final class TabletTextUtils {
	private TabletTextUtils() {

	}

	public static String[] split(String text, TabletFont font, int width, boolean justify) {
		List<String> lines = new ArrayList<String>();

		int x = 0;
		String line = "";
		boolean first = true;

		for (String s : text.split(" ")) {
			String ts = first ? s : " " + s;
			int w = font.getStringWidth(ts);
			if (x + w > width) {
				x = 0;
				ts = s;
				lines.add(line);
				line = "";
			}
			x += w;
			line += ts;
			first = false;
		}
		if (line.length() > 0) {
			lines.add(line);
		}
		return lines.toArray(new String[lines.size()]);
	}


}
