package buildcraft.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import buildcraft.api.mj.MjBattery;

public class ReflectMjAPI {

	public static class BatteryField {
		public Field field;
		public MjBattery battery;

		public double getEnergyRequested (Object obj) {
			try {
				double contained = field.getDouble(obj);

				double left = contained + battery.maxReceivedPerCycle() > battery
						.maxCapacity() ? battery.maxCapacity() - contained : battery
						.maxReceivedPerCycle();

				if (left > 0) {
					return left;
				} else {
					return battery.miniumConsumption();
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return 0;
		}
	}

	static Map <Class, BatteryField> MjBatteries = new HashMap <Class, BatteryField> ();

	public static BatteryField getMjBattery (Class c) {
		if (!MjBatteries.containsKey(c)) {
			for (Field f : c.getFields()) {
				MjBattery battery = f.getAnnotation (MjBattery.class);

				if (battery != null) {
					if (!f.getType().equals(double.class)) {
						throw new RuntimeException("MjBattery need to be of type double");
					} else {
						BatteryField bField = new BatteryField();
						bField.field = f;
						bField.battery = battery;

						return MjBatteries.put(c, bField);
					}
				}
			}

			return MjBatteries.put(c, null);
		} else {
			return MjBatteries.get(c);
		}
	}

}
