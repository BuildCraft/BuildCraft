package buildcraft.core.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
@SideOnly(Side.CLIENT)
public class GuiBetterButton extends GuiButton {

	public static final String BUTTON_TEXTURES = "/gfx/buildcraft/gui/buttons.png";

	public GuiBetterButton(int id, int x, int y, String label) {
		this(id, x, y, 200, 20, label);
	}

	public GuiBetterButton(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, label);

	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
