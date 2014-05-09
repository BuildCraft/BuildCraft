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
import java.util.logging.Level;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;

/**
 * The class MjAPI provides services to the Minecraft Joules power framework.
 * BuildCraft implements a default power model on top of this, the "kinesis"
 * power model. Third party mods may provide they own version of Minecraft
 * Joules batteries and provide different models.
 */
public final class MjAPI {
	public static final String DEFAULT_POWER_FRAMEWORK = "buildcraft.kinesis";
	private static Map<Class, BatteryField> mjBatteries = new HashMap<Class, BatteryField>();
	private static Map<String, Class<? extends BatteryObject>> mjBatteryKinds = new HashMap<String, Class<? extends BatteryObject>>();

	/**
	 * Deactivate constructor
	 */
	private MjAPI() {
	}

	/**
	 * Returns the default battery related to the object given in parameter. For
	 * performance optimization, it's good to cache this object in the providing
	 * power framework if possible.
	 */
	public static IBatteryObject getMjBattery(Object o) {
		return getMjBattery(o, DEFAULT_POWER_FRAMEWORK);
	}

	/**
	 * Returns the battery related to the object given in parameter. For
	 * performance optimization, it's good to cache this object in the providing
	 * power framework if possible.
	 */
	public static IBatteryObject getMjBattery(Object o, String kind) {
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
		} else if (!mjBatteryKinds.containsKey(kind)) {
			return null;
		} else if (f.kind == BatteryKind.Value) {
			try {
				BatteryObject obj = mjBatteryKinds.get(kind).newInstance();
				obj.obj = o;
				obj.energyStored = f.field;
				obj.batteryData = f.battery;

				return obj;
			} catch (InstantiationException e) {
				BCLog.logger.log(Level.WARNING, "can't instantiate class for energy kind \"" + kind + "\"");

				return null;
			} catch (IllegalAccessException e) {
				BCLog.logger.log(Level.WARNING, "can't instantiate class for energy kind \"" + kind + "\"");

				return null;
			}
		} else {
			try {
				return getMjBattery(f.field.get(o));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static IBatteryObject[] getAllMjBatteries(Object o) {
		IBatteryObject[] result = new IBatteryObject[mjBatteries.size()];

		int id = 0;

		for (String kind : mjBatteryKinds.keySet()) {
			result[id] = getMjBattery(o, kind);
			id++;
		}

		return result;
	}

	public static void registerMJBatteryKind(String kind, Class<? extends BatteryObject> clas) {
		if (!mjBatteryKinds.containsKey(kind)) {
			mjBatteryKinds.put(kind, clas);
		} else {
			BCLog.logger.log(Level.WARNING,
					"energy kind \"" + kind + "\" already registered with " + clas.getCanonicalName());
		}
	}

	private enum BatteryKind {
		Value, Container
	}

	private static class BatteryField {
		public Field field;
		public MjBattery battery;
		public BatteryKind kind;
	}

	private static BatteryField getMjBatteryField(Class c) {
		BatteryField bField = mjBatteries.get(c);

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

					mjBatteries.put(c, bField);

					return bField;
				}
			}

			mjBatteries.put(c, null);
		}

		return bField;
	}

	static {
		mjBatteryKinds.put(MjAPI.DEFAULT_POWER_FRAMEWORK, BatteryObject.class);
	}

}
