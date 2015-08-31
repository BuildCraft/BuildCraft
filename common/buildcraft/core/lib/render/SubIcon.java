package buildcraft.core.lib.render;

import net.minecraft.util.IIcon;

public class SubIcon implements IIcon {
	private final IIcon icon;
	private float u, v;
	private final int w, h;
	private float uScale, vScale;
	private int iw, ih;

	public SubIcon(IIcon icon, int u, int v, int size) {
		this(icon, u, v, 16, 16, size);
	}

	public SubIcon(IIcon icon, int u, int v, int w, int h, int size) {
		iw = size;
		ih = size;
		this.icon = icon;
		this.uScale = icon.getMaxU() - icon.getMinU();
		this.vScale = icon.getMaxV() - icon.getMinV();
		this.u = icon.getMinU() + (this.uScale * u / iw);
		this.v = icon.getMinV() + (this.vScale * v / ih);
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
		return u;
	}

	@Override
	public float getMaxU() {
		return u + (uScale * w / iw);
	}

	@Override
	public float getInterpolatedU(double uu) {
		return u + (uScale * (float) uu / (float) iw);
	}

	@Override
	public float getMinV() {
		return v;
	}

	@Override
	public float getMaxV() {
		return v + (vScale * h / ih);
	}

	@Override
	public float getInterpolatedV(double vv) {
		return v + (vScale * (float) vv / (float) ih);
	}

	@Override
	public String getIconName() {
		return icon.getIconName();
	}
}
