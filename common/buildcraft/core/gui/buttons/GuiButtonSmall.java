package buildcraft.core.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
@SideOnly(Side.CLIENT)
public class GuiButtonSmall extends GuiBetterButton {

	public GuiButtonSmall(int i, int x, int y, String s) {
		this(i, x, y, 200, s);
	}

	public GuiButtonSmall(int i, int x, int y, int w, String s) {
		super(i, x, y, w, StandardButtonTextureSets.SMALL_BUTTON, s);
	}
}
