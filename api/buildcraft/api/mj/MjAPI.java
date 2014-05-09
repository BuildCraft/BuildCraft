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
import java.util.HashMap;
import java.util.Map;

import buildcraft.api.core.JavaTools;

public final class MjAPI {
	private static Map<Class, BatteryField> MjBatteries = new HashMap<Class, BatteryField>();

	private enum BatteryKind {
		Value, Container
	}

	private static class BatteryField {
		Field field;
		MjBattery battery;
		BatteryKind kind;
	}

	public static class BatteryObject implements IBatteryObject {
		private Field f;
		private Object o;
		private MjBattery b;

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

	/**
	 * Deactivate constructor
	 */
	private MjAPI() {
	}

	public static IBatteryObject getMjBattery(Object o) {
		if (o == null) {
			return null;
		}

		if (o instanceof IBatteryProvider) {
			IBatteryObject battery = ((IBatteryProvider) o).getMjBattery();
			if (battery != null) {
				return battery;
			}
		}

		BatteryField f = getMjBatteryField(o.getClass());

		if (f == null) {
			return null;
		} else if (f.kind == BatteryKind.Value) {
			BatteryObject obj = new BatteryObject();
			obj.o = o;
			obj.f = f.field;
			obj.b = f.battery;
			return obj;
		} else {
			try {
				return getMjBattery(f.field.get(o));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private static BatteryField getMjBatteryField(Class c) {
		BatteryField bField = MjBatteries.get(c);
		if (bField == null) {
			for (Field f : JavaTools.getAllFields(c)) {
				MjBattery battery = f.getAnnotation(MjBattery.class);

				if (battery != null) {
					f.setAccessible(true);
					bField = new BatteryField();
					bField.field = f;
					bField.battery = battery;

					if (double.class.equals(f.getType())) {
						bField.kind = BatteryKind.Value;
					} else if (f.getType().isPrimitive()) {
						throw new RuntimeException(
								"MJ battery needs to be object or double type");
					} else {
						bField.kind = BatteryKind.Container;
					}

					MjBatteries.put(c, bField);

					return bField;
				}
			}
			MjBatteries.put(c, null);
		}
		return bField;
	}

}
