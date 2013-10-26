/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import buildcraft.core.Version;
import cpw.mods.fml.common.FMLLog;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BCLog {

	public static final Logger logger = Logger.getLogger("Buildcraft");

	public static void initLog() {
		logger.setParent(FMLLog.getLogger());
		logger.info("Starting BuildCraft " + Version.getVersion());
		logger.info("Copyright (c) SpaceToad, 2011");
		logger.info("http://www.mod-buildcraft.com");
	}

	public static void logErrorAPI(String mod, Throwable error, Class classFile) {
		StringBuilder msg = new StringBuilder(mod);
		msg.append(" API error, please update your mods. Error: ").append(error);
		StackTraceElement[] stackTrace = error.getStackTrace();
		if (stackTrace.length > 0)
			msg.append(", ").append(stackTrace[0]);
		logger.log(Level.SEVERE, msg.toString());

		if (classFile != null) {
			msg = new StringBuilder(mod);
			msg.append(" API error: ").append(classFile.getSimpleName()).append(" is loaded from ").append(classFile.getProtectionDomain().getCodeSource().getLocation());
			logger.log(Level.SEVERE, msg.toString());
		}
	}
}
