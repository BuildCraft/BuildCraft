/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaTools {
	public static double bounds(double value, double min, double max) {
		return Math.max(min, Math.min(value, max));
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static int[] concat(int[] first, int[] second) {
		int[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static float[] concat(float[] first, float[] second) {
		float[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public <T> T[] concatenate (T[] a, T[] b) {
	    int aLen = a.length;
	    int bLen = b.length;

	    @SuppressWarnings("unchecked")
		T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
	    System.arraycopy(a, 0, c, 0, aLen);
	    System.arraycopy(b, 0, c, aLen, bLen);

	    return c;
	}

	public static List<Field> getAllFields(Class<?> clas) {
	    List<Field> result = new ArrayList<Field>();

	    Class<?> current = clas;

	    while (current != null && current != Object.class) {
			Collections.addAll(result, current.getDeclaredFields());

	        current = current.getSuperclass();
	    }

	    return result;
	}

	public static List<Method> getAllMethods(Class<?> clas) {
	    List<Method> result = new ArrayList<Method>();

	    Class<?> current = clas;

	    while (current != null && current != Object.class) {
			Collections.addAll(result, current.getDeclaredMethods());

	        current = current.getSuperclass();
	    }

	    return result;
	}

	public static String surroundWithQuotes(String stringToSurroundWithQuotes) {
		return String.format("\"%s\"", stringToSurroundWithQuotes);
	}

	public static String stripSurroundingQuotes(String stringToStripQuotes) {
		return stringToStripQuotes.replaceAll("^\"|\"$", "");
	}
}
