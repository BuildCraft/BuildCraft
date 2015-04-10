package buildcraft.core.tablet;

import java.util.ArrayList;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import buildcraft.api.tablet.ITablet;
import buildcraft.api.tablet.TabletBitmap;
import buildcraft.api.tablet.TabletProgram;
import buildcraft.core.tablet.utils.TabletFont;
import buildcraft.core.tablet.utils.TabletFontManager;

public class TabletProgramMenu extends TabletProgram {
	private final ITablet tablet;
	private boolean init = false;
	private float t = 0.0F;

	public TabletProgramMenu(ITablet tablet) {
		this.tablet = tablet;
	}

	public String[] split(String text, TabletFont font, int width) {
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

	public void tick(float time) {
		t+=time;
		if (!init && t > 2 && tablet.getSide() == Side.CLIENT) {
			TabletBitmap bitmap = new TabletBitmap(244, 306);
			try {
				TabletFont font = TabletFontManager.INSTANCE.register("DejaVu11", TabletProgramMenu.class.getClassLoader().getResourceAsStream("assets/buildcraftcore/tablet/11.pf2"));
				String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque condimentum, nulla in tincidunt finibus, dolor enim condimentum felis, vitae vulputate lorem turpis nec purus. Nulla iaculis sed turpis in lacinia. Aliquam consectetur leo sit amet placerat blandit. Maecenas euismod magna eros, quis efficitur quam luctus mollis. Nulla facilisi. Quisque tempor turpis ipsum, ut auctor diam hendrerit dictum. Phasellus id viverra purus. Ut sagittis felis eu odio sagittis, vitae mollis felis feugiat. Morbi mi elit, varius id fringilla vel, vehicula ac risus. Curabitur aliquam orci at mollis posuere. Nam vitae neque tincidunt orci rhoncus rutrum.";
				int y = 0;
				String[] lines = split(lorem, font, 244);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i];
					String[] words = line.split(" ");
					float justifyValue = 0;
					if (i < lines.length - 1) {
						int widthNoSpaces = 0;
						for (String s : words) {
							widthNoSpaces += font.getStringWidth(s);
						}
						justifyValue = (244 - widthNoSpaces) / (float) (words.length - 1);
					}
					float x = 0;
					for (String s: words) {
						x += font.draw(bitmap, s, (int) x, y, 7);
						x += justifyValue;
					}
					y += font.getHeight() + 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			tablet.refreshScreen(bitmap);

			init = true;
		}
	}
}