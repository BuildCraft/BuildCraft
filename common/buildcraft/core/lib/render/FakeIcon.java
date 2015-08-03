package buildcraft.core.lib.render;

import net.minecraft.util.IIcon;

public class FakeIcon implements IIcon {
	private final int w, h;
	private final float minU, maxU, minV, maxV;

	public FakeIcon(float minU, float maxU, float minV, float maxV, int w, int h) {
		this.minU = minU;
		this.minV = minV;
		this.maxU = maxU;
		this.maxV = maxV;
		this.w = w;
		this.h = h;
	}

	@Override
	public int getIconWidth() {
		return w;
	}

	@Override
	public int getIconHeight() {
		return h;
	}

	@Override
	public float getMinU() {
		return minU;
	}

	@Override
	public float getMaxU() {
		return maxU;
	}

	@Override
	public float getInterpolatedU(double uu) {
		return (float) (minU + (uu * (maxU - minU) / 16.0));
	}

	@Override
	public float getMinV() {
		return minV;
	}

	@Override
	public float getMaxV() {
		return maxV;
	}

	@Override
	public float getInterpolatedV(double uu) {
		return (float) (minV + (uu * (maxV - minV) / 16.0));
	}

	@Override
	public String getIconName() {
		return "FakeIcon";
	}
}
