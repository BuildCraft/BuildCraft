/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

public final class SessionVars {

	@SuppressWarnings("rawtypes")
	private static Class openedLedger;

	/**
	 * Deactivate constructor
	 */
	private SessionVars() {
	}

	@SuppressWarnings("rawtypes")
	public static void setOpenedLedger(Class ledgerClass) {
		openedLedger = ledgerClass;
	}

	@SuppressWarnings("rawtypes")
	public static Class getOpenedLedger() {
		return openedLedger;
	}
}
