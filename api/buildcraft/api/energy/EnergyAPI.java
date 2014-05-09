/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.energy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import buildcraft.api.core.JavaTools;

public final class EnergyAPI {

	public static final String batteryChannelMJ = "BuildCraftMJ";
	public static final String genericObjectChannel = "GenericObjectContainerChannel";

	private static Map<Class<?>, Map<String, BatteryField>> batteries = new HashMap<Class<?>, Map<String, BatteryField>>();

	private enum BatteryKind {
		Value, Container
	}

	private static class BatteryField {
		Field field;
		EnergyBattery battery;
		BatteryKind kind;
	}

	public interface IBatteryProvider {
		BatteryObject getBattery(final String channel);
	}

	public static class BatteryObject {
		private Field f;
		private Object o;
		private EnergyBattery b;

		/**
		 * @return Current energy requirement for keeping machine state
		 */
		public double getEnergyRequested() {
			try {
				return Math.max(Math.min(b.maxCapacity() - f.getDouble(o), b.maxReceivedPerCycle()), b.minimumConsumption());
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}

		/**
		 * Add energy to this battery
		 *
		 * @param energy Energy amount
		 * @return Used energy
		 */
		public double addEnergy(final double energy) {
			return addEnergy(energy, false);
		}

		/**
		 * Add energy to this battery
		 *
		 * @param energy           Energy amount
		 * @param ignoreCycleLimit Force add all energy even if "maxReceivedPerCycle" limit is reached
		 * @return Used energy
		 */
		public double addEnergy(final double energy, final boolean ignoreCycleLimit) {
			try {
				final double contained = f.getDouble(o);
				double maxAccepted = b.maxCapacity() - contained + b.minimumConsumption();
				if (!ignoreCycleLimit && maxAccepted > b.maxReceivedPerCycle()) {
					maxAccepted = b.maxReceivedPerCycle();
				}
				final double used = Math.min(maxAccepted, energy);
				if (used > 0) {
					f.setDouble(o, Math.min(contained + used, b.maxCapacity()));
					return used;
				}
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}

		/**
		 * @return Current stored energy amount in this battery
		 */
		public double getEnergyStored() {
			try {
				return f.getDouble(o);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return 0;
			}
		}

		/**
		 * Set current stored energy amount.
		 * Doesn't use it for your machines! Decrease your battery field directly.
		 *
		 * @param mj New energy amount
		 * 
		 */
		public void setEnergyStored(final double energy) {
			try {
				f.setDouble(o, energy);
			} catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Can be overrided via {@link #reconfigure(double, double, double)}
		 *
		 * @return Maximal energy amount for this battery.
		 */
		public double maxCapacity() {
			return b.maxCapacity();
		}

		/**
		 * Can be overrided via {@link #reconfigure(double, double, double)}
		 *
		 * @return Minimal energy amount for keep your machine in active state
		 */
		public double minimumConsumption() {
			return b.minimumConsumption();
		}

		/**
		 * Can be overrided via {@link #reconfigure(double, double, double)}
		 *
		 * @return Maximal energy received per one tick
		 */
		public double maxReceivedPerCycle() {
			return b.maxReceivedPerCycle();
		}
		
		/**
		 * @return The energy type this battery carries, e.x MJ
		 */
		public String getChannel() {
			return b.energyChannel();
		}

		/**
		 * Allow to dynamically reconfigure your battery.
		 * Usually it's not very good change battery parameters for already present machines, but if you want...
		 *
		 * @param maxCapacity         {@link #maxCapacity()}
		 * @param maxReceivedPerCycle {@link #maxReceivedPerCycle()}
		 * @param minimumConsumption  {@link #minimumConsumption()}
		 * @return Current battery object instance
		 */
		public BatteryObject reconfigure(final double maxCapacity, final double maxReceivedPerCycle,
				final double minimumConsumption, final String channel) {
			b = new EnergyBattery() {
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
					return EnergyBattery.class;
				}

				@Override
				public String energyChannel() {
					return channel;
				}
			};
			return this;
		}
	}

	/**
	 * Deactivate constructor
	 */
	private EnergyAPI() {}

	public static BatteryObject getBattery(final Object o, final String channel) {
		if ((o == null) || (channel == null) || ("".equals(channel))) {
			return null;
		}
		
		if (channel.equals(genericObjectChannel)) {
			throw new SecurityException("Trying to access the generic container channel!");
		}

		if (o instanceof IBatteryProvider) {
			final BatteryObject battery = ((IBatteryProvider) o).getBattery(channel);
			if (battery != null) {
				return battery;
			}
		}

		final BatteryField f = getMjBatteryField(o.getClass(), channel);

		if (f == null) {
			try {
				final BatteryField allChannelFields = getMjBatteryField(o.getClass(), genericObjectChannel);
				
				if (allChannelFields == null) {
					return null;
				} else if (allChannelFields.kind == BatteryKind.Value) {
					throw new RuntimeException("Theres a type of Value on an Object field!");
				}
				
				return getBattery(allChannelFields.field.get(o), channel);
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		} else if (f.kind == BatteryKind.Value) {
			final BatteryObject obj = new BatteryObject();
			obj.o = o;
			obj.f = f.field;
			obj.b = f.battery;

			return obj;
		} else if (f.kind == BatteryKind.Container) {
			System.out.println(o.getClass());
			throw new RuntimeException("Theres a type of Container on a Double field!");
		} else {
			return null;
		}
	}

	private static BatteryField getMjBatteryField(final Class<?> c, final String channel) {
		BatteryField bField = getBatteryFromMap(c, channel);

		if (bField == null) {
			for (final Field f : JavaTools.getAllFields(c)) {
				final EnergyBattery battery = f.getAnnotation(EnergyBattery.class);

				if (battery != null) {
					f.setAccessible(true);
					bField = new BatteryField();
					bField.field = f;
					bField.battery = battery;
					boolean isContainer = false;

					if (double.class.equals(f.getType())) {
						if (!battery.energyChannel().equals(channel)) { continue }

					} else if (f.getType().isPrimitive()) {
						throw new RuntimeException(
								"MJ battery needs to be object or double type");
					} else {
						if (!battery.energyChannel().equals(genericObjectChannel)) { continue }

						isContainer = true;
					}

					bField.kind = isContainer ? BatteryKind.Container : BatteryKind.Value;

					insertNewBattery(c, bField, isContainer ? genericObjectChannel : channel);

					return isContainer ? null : bField;
				}
			}

			batteries.put(c, null);
		}

		return bField;

	}
	
	private static void insertNewBattery(final Class<?> c, final BatteryField bField, final String channel) {

		//null check and initialize
		Map<String, BatteryField> allChannelsForClass = batteries.get(c);

		if (allChannelsForClass == null) {
			allChannelsForClass = new HashMap<String, BatteryField>();
			batteries.put(c, allChannelsForClass);
		}

		allChannelsForClass.put(channel, bField);
	}
	
	private static BatteryField getBatteryFromMap(final Class<?> c, final String channel) {

		//null check
		Map<String, BatteryField> allChannelsForClass = batteries.get(c);

		if (allChannelsForClass == null) {
			return null; //don't initialize
		}

		return allChannelsForClass.get(channel);

	}

}
