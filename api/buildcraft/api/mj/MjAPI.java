/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import buildcraft.api.core.JavaTools;

public class MjAPI {

	private enum BatteryKind {
		Value, Container
	}

	private static class BatteryField {
		public Field field;
		public MjBattery battery;
		public BatteryKind kind;

		public double getEnergyRequested (Object obj) {
			try {
				double contained = field.getDouble(obj);

				double left = contained + battery.maxReceivedPerCycle() > battery
						.maxCapacity() ? battery.maxCapacity() - contained : battery
						.maxReceivedPerCycle();

				if (left > 0) {
					return left;
				} else {
					return battery.minimumConsumption();
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}
	}

	public static class BatteryObject {
		BatteryField f;
		Object o;

		public double getEnergyRequested () {
			return f.getEnergyRequested(o);
		}

		public double addEnergy(double watts) {
			try {
				double e = f.field.getDouble(o);
				double max = f.battery.maxCapacity();

				double used = 0;

				if (e + watts <= max) {
					used = watts;
				} else {
					used = max - e;
				}

				f.field.setDouble(o, e + used);

				return used;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}

		public double getEnergyStored() {
			try {
				return f.field.getDouble(o);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return 0;
			}
		}

		public double maxCapacity() {
			return f.battery.maxCapacity();
		}

		public double minimumConsumption() {
			return f.battery.minimumConsumption();
		}

		public double maxReceivedPerCycle() {
			return f.battery.maxReceivedPerCycle();
		}
	}

	public static BatteryObject getMjBattery (Object o) {
		if (o == null) {
			return null;
		}

		BatteryField f = getMjBattery (o.getClass());

		if (f == null) {
			return null;
		} else if (f.kind == BatteryKind.Value) {
			BatteryObject obj = new BatteryObject();
			obj.o = o;
			obj.f = f;

			return obj;
		} else {
			try {
				return getMjBattery(f.field.get(o));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	static Map <Class, BatteryField> MjBatteries = new HashMap <Class, BatteryField> ();

	private static BatteryField getMjBattery (Class c) {
		if (!MjBatteries.containsKey(c)) {
			for (Field f : JavaTools.getAllFields(c)) {
				MjBattery battery = f.getAnnotation (MjBattery.class);

				if (battery != null) {
					f.setAccessible(true);
					BatteryField bField = new BatteryField();
					bField.field = f;
					bField.battery = battery;

					if (f.getType().equals(double.class)) {
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
