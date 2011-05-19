package net.minecraft.src.buildcraft.core;

public class DefaultAreaProvider implements IAreaProvider {

	int xMin, yMin, zMin, xMax, yMax, zMax;
	
	public DefaultAreaProvider(int xMin, int yMin, int zMin, int xMax,
			int yMax, int zMax) {
		
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}
	
	@Override
	public int xMin() {
		return xMin;
	}

	@Override
	public int yMin() {
		return yMin;
	}

	@Override
	public int zMin() {
		return zMin;
	}

	@Override
	public int xMax() {
		return xMax;
	}

	@Override
	public int yMax() {
		return yMax;
	}

	@Override
	public int zMax() {
		return zMax;
	}

}
