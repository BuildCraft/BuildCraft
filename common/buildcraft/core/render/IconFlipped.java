package buildcraft.core.render;

import net.minecraft.util.IIcon;

/**
 * @author dmillerw
 */
public class IconFlipped implements IIcon {

	private final IIcon icon;

	private boolean flipU, flipV;

	public IconFlipped(IIcon icon) {
		this.icon = icon;
	}

	public IconFlipped flipU(boolean flip) {
		this.flipU = flip;
		return this;
	}

	public IconFlipped flipV(boolean flip) {
		this.flipV = flip;
		return this;
	}

	@Override
	public int getIconWidth() {
		return flipU ? -this.getIconWidth() : this.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return flipV ? -this.getIconHeight() : this.getIconHeight();
	}

	@Override
	public float getMinU() {
		return flipU ? this.icon.getMaxU() : this.icon.getMinU();
	}

	@Override
	public float getMaxU() {
		return flipU ? this.icon.getMinU() : this.icon.getMaxU();
	}

	@Override
	public float getInterpolatedU(double value) {
		float f = getMaxU() - getMinU();
		return getMinU() + f * (((float) value / 16.0F));
	}

	@Override
	public float getMinV() {
		return flipV ? this.icon.getMaxV() : this.icon.getMinV();
	}

	@Override
	public float getMaxV() {
		return flipV ? this.icon.getMinV() : this.icon.getMaxV();
	}

	@Override
	public float getInterpolatedV(double value) {
		float f = getMaxV() - getMinV();
		return getMinV() + f * (((float) value / 16.0F));
	}

	@Override
	public String getIconName() {
		return this.icon.getIconName();
	}

}
