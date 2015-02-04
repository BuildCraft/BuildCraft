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

public abstract class RobotManager {

	public static ArrayList<Class<? extends AIRobot>> aiRobots = new ArrayList<Class<? extends AIRobot>>();
	private static Map<Class<? extends AIRobot>, String> aiRobotsNames =
			new HashMap<Class<? extends AIRobot>, String>();
	private static Map<String, Class<? extends AIRobot>> aiRobotsByNames =
			new HashMap<String, Class<? extends AIRobot>>();
	private static Map<String, Class<? extends AIRobot>> aiRobotsByLegacyClassNames =
			new HashMap<String, Class<? extends AIRobot>>();

	private static Map<Class<? extends ResourceId>, String> resourceIdNames =
			new HashMap<Class<? extends ResourceId>, String>();
	private static Map<String, Class<? extends ResourceId>> resourceIdByNames =
			new HashMap<String, Class<? extends ResourceId>>();
	private static Map<String, Class<? extends ResourceId>> resourceIdLegacyClassNames =
			new HashMap<String, Class<? extends ResourceId>>();

	public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name) {
		registerAIRobot(aiRobot, name, null);
	}

	public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name, String legacyClassName) {
		aiRobots.add(aiRobot);
		aiRobotsByNames.put(name, aiRobot);
		aiRobotsNames.put(aiRobot, name);
		if(legacyClassName != null) {
			aiRobotsByLegacyClassNames.put(legacyClassName, aiRobot);
		}
	}

	public static Class<?> getAIRobotByName(String aiRobotName) {
		return aiRobotsByNames.get(aiRobotName);
	}

	public static String getAIRobotName(Class<? extends AIRobot> aiRobotClass) {
		return aiRobotsNames.get(aiRobotClass);
	}

	public static Class<?> getAIRobotByLegacyClassName(String aiRobotLegacyClassName) {
		return aiRobotsByLegacyClassNames.get(aiRobotLegacyClassName);
	}

	public static void registerResourceId(Class<? extends ResourceId> resourceId, String name) {
		registerResourceId(resourceId, name, null);
	}

	public static void registerResourceId(Class<? extends ResourceId> resourceId, String name, String legacyClassName) {
		resourceIdByNames.put(name, resourceId);
		resourceIdNames.put(resourceId, name);
		if(legacyClassName != null) {
			resourceIdLegacyClassNames.put(legacyClassName, resourceId);
		}
	}

	public static Class<?> getResourceIdByName(String resourceIdName) {
		return resourceIdByNames.get(resourceIdName);
	}

	public static String getResourceIdName(Class<? extends ResourceId> resouceIdClass) {
		return resourceIdNames.get(resouceIdClass);
	}

	public static Class<?> getResourceIdByLegacyClassName(String resourceIdLegacyClassName) {
		return resourceIdLegacyClassNames.get(resourceIdLegacyClassName);
	}
}
