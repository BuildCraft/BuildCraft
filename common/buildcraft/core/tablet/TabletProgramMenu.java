package buildcraft.core.tablet;

import cpw.mods.fml.relauncher.Side;

import buildcraft.api.tablet.ITablet;
import buildcraft.api.tablet.TabletBitmap;
import buildcraft.api.tablet.TabletProgram;
import buildcraft.core.tablet.utils.TabletDrawUtils;
import buildcraft.core.tablet.utils.TabletFont;
import buildcraft.core.tablet.utils.TabletFontManager;
import buildcraft.core.tablet.utils.TabletTextUtils;

public class TabletProgramMenu extends TabletProgram {
	private final ITablet tablet;
	private boolean init = false;
	private float t = 0.0F;

	public TabletProgramMenu(ITablet tablet) {
		this.tablet = tablet;
	}

	public void tick(float time) {
		t += time;
		if (!init && t > 2 && tablet.getSide() == Side.CLIENT) {
			TabletBitmap bitmap = new TabletBitmap(244, 306);
			try {
				TabletFont font = TabletFontManager.INSTANCE.register("DejaVu11", TabletProgramMenu.class.getClassLoader().getResourceAsStream("assets/buildcraftcore/tablet/11.pf2"));
				String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque condimentum, nulla in tincidunt finibus, dolor enim condimentum felis, vitae vulputate lorem turpis nec purus. Nulla iaculis sed turpis in lacinia. Aliquam consectetur leo sit amet placerat blandit. Maecenas euismod magna eros, quis efficitur quam luctus mollis. Nulla facilisi. Quisque tempor turpis ipsum, ut auctor diam hendrerit dictum. Phasellus id viverra purus. Ut sagittis felis eu odio sagittis, vitae mollis felis feugiat. Morbi mi elit, varius id fringilla vel, vehicula ac risus. Curabitur aliquam orci at mollis posuere.";
				TabletDrawUtils.drawRect(bitmap, 4, 4, 236, 280, 7);
				int hxo = (244 - font.getStringWidth("Page 1")) / 2;
				font.draw(bitmap, "Page 2/4", hxo, 287, 5);
				int xo = 8;
				int y = 8;
				int w = 228;
				String[] lines = TabletTextUtils.split(lorem, font, w, false);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i];
					String[] words = line.split(" ");
					float justifyValue = 0;
					if (i < lines.length - 1) {
						int widthNoSpaces = 0;
						for (String s : words) {
							widthNoSpaces += font.getStringWidth(s);
						}
						justifyValue = (w - widthNoSpaces) / (float) (words.length - 1);
					}
					float x = xo;
					for (String s : words) {
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