/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AIRobotManager {

	public static ArrayList<Class<? extends AIRobot>> aiRobots = new ArrayList<Class<? extends AIRobot>>();
	private static Map<Class<? extends AIRobot>, String> aiRobotsNames =
			new HashMap<Class<? extends AIRobot>, String>();
	private static Map<String, Class<? extends AIRobot>> aiRobotsByNames =
			new HashMap<String, Class<? extends AIRobot>>();
	private static Map<String, Class<? extends AIRobot>> aiRobotsByLegacyClassNames =
			new HashMap<String, Class<? extends AIRobot>>();

	public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name) {
		registerAIRobot(aiRobot, name, null);
	}
	
	public static void registerAIRobot(Class<? extends AIRobot> pluggable, String name, String legacyClassName) {
		aiRobots.add(pluggable);
		aiRobotsByNames.put(name, pluggable);
		aiRobotsNames.put(pluggable, name);
		if(legacyClassName != null) {
			aiRobotsByLegacyClassNames.put(legacyClassName, pluggable);
		}
	}

	public static Class<?> getAIRobotByName(String pluggableName) {
		return aiRobotsByNames.get(pluggableName);
	}

	public static String getAIRobotName(Class<? extends AIRobot> aClass) {
		return aiRobotsNames.get(aClass);
	}

	public static Class<?> getAIRobotByLegacyClassName(String string) {
		return aiRobotsByLegacyClassNames.get(string);
	}
}
