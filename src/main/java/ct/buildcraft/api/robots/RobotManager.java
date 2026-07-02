/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL.
 */
package ct.buildcraft.api.robots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.BCLog;

public abstract class RobotManager {
    public static IRobotRegistryProvider registryProvider;
    public static final ArrayList<Class<? extends AIRobot>> aiRobots = new ArrayList<>();

    private static final Map<Class<? extends AIRobot>, String> AI_ROBOT_NAMES = new HashMap<>();
    private static final Map<String, Class<? extends AIRobot>> AI_ROBOTS_BY_NAMES = new HashMap<>();
    private static final Map<String, Class<? extends AIRobot>> AI_ROBOTS_BY_LEGACY_CLASS_NAMES = new HashMap<>();

    private static final Map<Class<? extends ResourceId>, String> RESOURCE_ID_NAMES = new HashMap<>();
    private static final Map<String, Class<? extends ResourceId>> RESOURCE_IDS_BY_NAMES = new HashMap<>();
    private static final Map<String, Class<? extends ResourceId>> RESOURCE_ID_LEGACY_CLASS_NAMES = new HashMap<>();

    private static final Map<Class<? extends DockingStation>, String> DOCKING_STATION_NAMES = new HashMap<>();
    private static final Map<String, Class<? extends DockingStation>> DOCKING_STATIONS_BY_NAMES = new HashMap<>();

    static {
        registerResourceId(ResourceIdBlock.class, "resourceIdBlock", "buildcraft.core.robots.ResourceIdBlock");
        registerResourceId(ResourceIdRequest.class, "resourceIdRequest", "buildcraft.core.robots.ResourceIdRequest");
    }

    public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name) {
        registerAIRobot(aiRobot, name, null);
    }

    public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name, @Nullable String legacyClassName) {
        if (AI_ROBOTS_BY_NAMES.containsKey(name)) {
            BCLog.logger.info("Overriding " + AI_ROBOTS_BY_NAMES.get(name).getName() + " with " + aiRobot.getName());
        }

        try {
            aiRobot.getConstructor(EntityRobotBase.class);
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException("AI class " + aiRobot.getName() + " lacks an EntityRobotBase constructor.", exception);
        }

        aiRobots.add(aiRobot);
        AI_ROBOTS_BY_NAMES.put(name, aiRobot);
        AI_ROBOT_NAMES.put(aiRobot, name);
        if (legacyClassName != null) {
            AI_ROBOTS_BY_LEGACY_CLASS_NAMES.put(legacyClassName, aiRobot);
        }
    }

    @Nullable
    public static Class<?> getAIRobotByName(String aiRobotName) {
        return AI_ROBOTS_BY_NAMES.get(aiRobotName);
    }

    @Nullable
    public static String getAIRobotName(Class<? extends AIRobot> aiRobotClass) {
        return AI_ROBOT_NAMES.get(aiRobotClass);
    }

    @Nullable
    public static Class<?> getAIRobotByLegacyClassName(String aiRobotLegacyClassName) {
        return AI_ROBOTS_BY_LEGACY_CLASS_NAMES.get(aiRobotLegacyClassName);
    }

    public static void registerResourceId(Class<? extends ResourceId> resourceId, String name) {
        registerResourceId(resourceId, name, null);
    }

    public static void registerResourceId(Class<? extends ResourceId> resourceId, String name, @Nullable String legacyClassName) {
        RESOURCE_IDS_BY_NAMES.put(name, resourceId);
        RESOURCE_ID_NAMES.put(resourceId, name);
        if (legacyClassName != null) {
            RESOURCE_ID_LEGACY_CLASS_NAMES.put(legacyClassName, resourceId);
        }
    }

    @Nullable
    public static Class<?> getResourceIdByName(String resourceIdName) {
        return RESOURCE_IDS_BY_NAMES.get(resourceIdName);
    }

    @Nullable
    public static String getResourceIdName(Class<? extends ResourceId> resourceIdClass) {
        return RESOURCE_ID_NAMES.get(resourceIdClass);
    }

    @Nullable
    public static Class<?> getResourceIdByLegacyClassName(String resourceIdLegacyClassName) {
        return RESOURCE_ID_LEGACY_CLASS_NAMES.get(resourceIdLegacyClassName);
    }

    public static void registerDockingStation(Class<? extends DockingStation> dockingStation, String name) {
        DOCKING_STATIONS_BY_NAMES.put(name, dockingStation);
        DOCKING_STATION_NAMES.put(dockingStation, name);
    }

    @Nullable
    public static Class<? extends DockingStation> getDockingStationByName(String dockingStationTypeName) {
        return DOCKING_STATIONS_BY_NAMES.get(dockingStationTypeName);
    }

    @Nullable
    public static String getDockingStationName(Class<? extends DockingStation> dockingStation) {
        return DOCKING_STATION_NAMES.get(dockingStation);
    }
}
