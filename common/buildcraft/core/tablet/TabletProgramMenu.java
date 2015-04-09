package buildcraft.core.tablet;

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

	public void tick(float time) {
		t+=time;
		if (!init && t > 2 && tablet.getSide() == Side.CLIENT) {
			TabletBitmap bitmap = new TabletBitmap(244, 306);
			try {
				TabletFont text = TabletFontManager.INSTANCE.register("DejaVu11", TabletProgramMenu.class.getClassLoader().getResourceAsStream("assets/buildcraftcore/tablet/11.pf2"));
				String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque condimentum, nulla in tincidunt finibus, dolor enim condimentum felis, vitae vulputate lorem turpis nec purus. Nulla iaculis sed turpis in lacinia. Aliquam consectetur leo sit amet placerat blandit. Maecenas euismod magna eros, quis efficitur quam luctus mollis. Nulla facilisi. Quisque tempor turpis ipsum, ut auctor diam hendrerit dictum. Phasellus id viverra purus. Ut sagittis felis eu odio sagittis, vitae mollis felis feugiat. Morbi mi elit, varius id fringilla vel, vehicula ac risus. Curabitur aliquam orci at mollis posuere. Nam vitae neque tincidunt orci rhoncus rutrum.";

				int x = 0;
				int y = 0;
				boolean first = true;

				for (String s : lorem.split(" ")) {
					String ts = first ? s : " " + s;
					int width = text.getStringWidth(ts);
					if (x + width > 244) {
						x = 0;
						y += text.getHeight() + 1;
						ts = s;
					}
					x += text.draw(bitmap, ts, x, y, 7);
					first = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			tablet.refreshScreen(bitmap);

			init = true;
		}
	}
}
