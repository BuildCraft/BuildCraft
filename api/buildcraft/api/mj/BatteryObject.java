/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.logging.Level;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;

/**
 * A battery object is a wrapper around a battery field in an object. This
 * battery field is of type double, and is the only piece of data specific to
 * this object. Others are class-wide.
 */
public class BatteryObject implements IBatteryObject {
	protected Field energyStored;
	protected Object obj;
	protected MjBattery batteryData;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getEnergyRequested() {
		try {
			return JavaTools.bounds(batteryData.maxCapacity() - energyStored.getDouble(obj),
					batteryData.minimumConsumption(), batteryData.maxReceivedPerCycle());
		} catch (IllegalAccessException e) {
			BCLog.logger.log(Level.WARNING, "can't get energy requested", e);
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
			double contained = energyStored.getDouble(obj);
			double maxAccepted = batteryData.maxCapacity() - contained + batteryData.minimumConsumption();
			if (!ignoreCycleLimit && maxAccepted > batteryData.maxReceivedPerCycle()) {
				maxAccepted = batteryData.maxReceivedPerCycle();
			}
			double used = Math.min(maxAccepted, mj);
			if (used > 0) {
				energyStored.setDouble(obj, Math.min(contained + used, batteryData.maxCapacity()));
				return used;
			}
		} catch (IllegalAccessException e) {
			BCLog.logger.log(Level.WARNING, "can't add energy", e);
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getEnergyStored() {
		try {
			return energyStored.getDouble(obj);
		} catch (IllegalAccessException e) {
			BCLog.logger.log(Level.WARNING, "can't get return energy stored", e);
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnergyStored(double mj) {
		try {
			energyStored.setDouble(obj, mj);
		} catch (IllegalAccessException e) {
			BCLog.logger.log(Level.WARNING, "can't set energy stored", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double maxCapacity() {
		return batteryData.maxCapacity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double minimumConsumption() {
		return batteryData.minimumConsumption();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double maxReceivedPerCycle() {
		return batteryData.maxReceivedPerCycle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BatteryObject reconfigure(final double maxCapacity, final double maxReceivedPerCycle, final double minimumConsumption) {
		batteryData = new MjBattery() {
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

			@Override
			public String kind() {
				return MjAPI.DEFAULT_POWER_FRAMEWORK;
			}
		};

		return this;
	}
}