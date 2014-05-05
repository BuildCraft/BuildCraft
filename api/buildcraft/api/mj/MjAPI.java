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
import buildcraft.api.power.PowerHandler;

public final class MjAPI {

	static Map<Class, BatteryField> MjBatteries = new HashMap<Class, BatteryField>();

	private enum BatteryKind {
		Value, Container
	}

	private static class BatteryField {
		public Field field;
		public MjBattery battery;
		public BatteryKind kind;
	}

	public static class BatteryObject {
		Field f;
		Object o;
		MjBattery b;

		public double getEnergyRequested() {
			try {
				double contained = f.getDouble(o);

				double left = contained + b.maxReceivedPerCycle() > b
						.maxCapacity() ? b.maxCapacity() - contained : b
						.maxReceivedPerCycle();

				if (left > 0) {
					return left;
				} else {
					return b.minimumConsumption();
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}

		public double addEnergy(double watts) {
			try {
				double e = f.getDouble(o);
				double max = b.maxCapacity();

				double used = e + watts <= max ? watts : max - e;

				f.setDouble(o, e + used);

				return used;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}

		public double getEnergyStored() {
			try {
				return f.getDouble(o);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return 0;
			}
		}

		public void setEnergyStored(double watts) {
			try {
				f.setDouble(o, watts);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		public double maxCapacity() {
			return b.maxCapacity();
		}

		public double minimumConsumption() {
			return b.minimumConsumption();
		}

		public double maxReceivedPerCycle() {
			return b.maxReceivedPerCycle();
		}

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

	public static BatteryObject getMjBattery(Object o) {
		if (o == null) {
			return null;
		}

		if (o.getClass() == PowerHandler.class) {
			return ((PowerHandler) o).getMjBattery();
		}

		BatteryField f = getMjBattery(o.getClass());

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

	private static BatteryField getMjBattery(Class c) {
		if (!MjBatteries.containsKey(c)) {
			for (Field f : JavaTools.getAllFields(c)) {
				MjBattery battery = f.getAnnotation(MjBattery.class);

				if (battery != null) {
					f.setAccessible(true);
					BatteryField bField = new BatteryField();
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

			return null;
		} else {
			return MjBatteries.get(c);
		}
	}

}
