package buildcraft.core.tablet;

import buildcraft.api.tablet.ITablet;
import buildcraft.api.tablet.TabletProgram;

public class TabletProgramMenu extends TabletProgram {
	private final ITablet tablet;
	private boolean init = false;
	private float t = 0.0F;

	public TabletProgramMenu(ITablet tablet) {
		this.tablet = tablet;
	}

	public void tick(float time) {
		t+=time;
		if (!init && t > 2) {
			init = true;
		}
	}
}
