package buildcraft.api.core;

import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyDouble implements IUnlistedProperty<Double> {
	private final String name;
	private final double min, max;

	public PropertyDouble(String name, double min, double max) {
		this.name = name;
		this.min = min;
		this.max = max;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isValid(Double value) {
		double v = value.doubleValue();
		return min <= v && v <= max;
	}

	@Override
	public Class<Double> getType() {
		return Double.class;
	}

	@Override
	public String valueToString(Double value) {
		return value.toString();
	}
}
