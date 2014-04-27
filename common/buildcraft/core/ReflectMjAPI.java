package buildcraft.core;

import buildcraft.api.mj.MjBattery;
import buildcraft.core.utils.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ReflectMjAPI {

	enum BatteryKind {
		Value, Container
	}

	private static class BatteryField {
		public Field field;
		public MjBattery battery;
		public BatteryKind kind;

		public double getEnergyRequested(Object obj) {
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

		public double getEnergyRequested() {
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

		public double minimumConsumption() {
			return f.battery.minimumConsumption();
		}

		public double maxReceivedPerCycle() {
			return f.battery.maxReceivedPerCycle();
		}
	}

	public static BatteryObject getMjBattery(Object o) {
		if (o == null) {
			return null;
		}

		BatteryField f = getMjBattery(o.getClass());

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

	static Map<Class, BatteryField> MjBatteries = new HashMap<Class, BatteryField>();

	private static BatteryField getMjBattery(Class c) {
		if (!MjBatteries.containsKey(c)) {
			for (Field f : Utils.getAllFields(c)) {
				MjBattery battery = f.getAnnotation(MjBattery.class);

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
