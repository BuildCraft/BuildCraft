package buildcraft.core.gui.buttons;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class ButtonTextureSet implements IButtonTextureSet {

	private final int x, y, height, width;

	public ButtonTextureSet(int x, int y, int height, int width) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}
}
