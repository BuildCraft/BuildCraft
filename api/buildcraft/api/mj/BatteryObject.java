package buildcraft.api.mj;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import buildcraft.api.core.JavaTools;

public class BatteryObject implements IBatteryObject {
	protected Field f;
	protected Object o;
	protected MjBattery b;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getEnergyRequested() {
		try {
			return JavaTools.bounds(b.maxCapacity() - f.getDouble(o), b.minimumConsumption(), b.maxReceivedPerCycle());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double addEnergy(double mj) {
		return addEnergy(mj, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double addEnergy(double mj, boolean ignoreCycleLimit) {
		try {
			double contained = f.getDouble(o);
			double maxAccepted = b.maxCapacity() - contained + b.minimumConsumption();
			if (!ignoreCycleLimit && maxAccepted > b.maxReceivedPerCycle()) {
				maxAccepted = b.maxReceivedPerCycle();
			}
			double used = Math.min(maxAccepted, mj);
			if (used > 0) {
				f.setDouble(o, Math.min(contained + used, b.maxCapacity()));
				return used;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getEnergyStored() {
		try {
			return f.getDouble(o);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnergyStored(double mj) {
		try {
			f.setDouble(o, mj);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double maxCapacity() {
		return b.maxCapacity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double minimumConsumption() {
		return b.minimumConsumption();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double maxReceivedPerCycle() {
		return b.maxReceivedPerCycle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BatteryObject reconfigure(final double maxCapacity, final double maxReceivedPerCycle, final double minimumConsumption) {
		b = new MjBattery() {
			@Override
			public double maxCapacity() {
				return maxCapacity;
			}

			@Override
			public double maxReceivedPerCycle() {
				return maxReceivedPerCycle;
			}

			@Override
			public double minimumConsumption() {
				return minimumConsumption;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return MjBattery.class;
			}
		};
		return this;
	}
}