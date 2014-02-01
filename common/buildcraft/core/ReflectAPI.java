package buildcraft.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ReflectAPI {

	public static String KEY_MJ_STORED = "_MJ_STORED";
	static Map <Class, Field> MAP_MJ_STORED = new HashMap <Class, Field> ();

	public static Field get_MJ_STORED (Class c) {
		if (!MAP_MJ_STORED.containsKey(c)) {
			for (Field f : c.getFields()) {
				if (f.getName().equals(KEY_MJ_STORED)) {
					return MAP_MJ_STORED.put(c, f);
				}
			}

			return MAP_MJ_STORED.put(c, null);
		} else {
			return MAP_MJ_STORED.get(c);
		}
	}

}
