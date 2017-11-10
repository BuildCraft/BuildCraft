/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import com.google.common.base.Splitter;

import net.minecraft.util.StatCollector;

public final class StringUtils {

	public static final Splitter newLineSplitter = Splitter.on("\\n");

	/**
	 * Deactivate constructor
	 */
	private StringUtils() {
	}

	public static String localize(String key) {
		return StatCollector.translateToLocal(key);
	}

	public static boolean canLocalize(String key) {
		return StatCollector.canTranslate(key);
	}
}