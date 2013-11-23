package buildcraft.core.gui.buttons;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public enum StandardButtonTextureSets implements IButtonTextureSet {

	LARGE_BUTTON(0, 0, 20, 200),
	SMALL_BUTTON(0, 80, 15, 200),
    LEFT_BUTTON(204, 0, 16, 10),
    RIGHT_BUTTON(214, 0, 16, 10);
	private final int x, y, height, width;

	private StandardButtonTextureSets(int x, int y, int height, int width) {
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
