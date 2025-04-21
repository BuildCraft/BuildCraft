package buildcraft.robotics;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.robotics.statements.ActionRobotWorkInArea;

import java.util.EnumMap;
import java.util.Map;

public class BCRoboticsSprites {
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_FILTER_TOOL;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_FILTER;
    public static final SpriteHolderRegistry.SpriteHolder TRIGGER_ROBOT_SLEEP;
    public static final Map<ActionRobotWorkInArea.AreaType, SpriteHolderRegistry.SpriteHolder> ACTION_ROBOT_AREA_TYPE;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_WAKEUP;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_ACCEPT_FLUIDS;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_ACCEPT_ITEMS;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_ROBOT_MANDATORY;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_ROBOT_FORBIDDEN;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_PROVIDE_FLUIDS;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_PROVIDE_ITEMS;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_MACHINE_REQUEST;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_REQUEST_ITEMS;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_GOTO_STATION;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_RESERVED;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_LINKED;
    public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_IN_STATION;

    static {
        ACTION_ROBOT_FILTER_TOOL = getHolder("triggers/action_robot_filter_tool");
        ACTION_ROBOT_FILTER = getHolder("triggers/action_robot_filter");
        TRIGGER_ROBOT_SLEEP = getHolder("triggers/trigger_robot_sleep");
        ACTION_ROBOT_AREA_TYPE = new EnumMap<>(ActionRobotWorkInArea.AreaType.class);
        for (ActionRobotWorkInArea.AreaType type : ActionRobotWorkInArea.AreaType.values()) {
            String tex = type.getSpriteLocation();
            ACTION_ROBOT_AREA_TYPE.put(type, getHolder(tex));
        }
        ACTION_ROBOT_WAKEUP = getHolder("triggers/action_robot_wakeup");
        ACTION_STATION_ACCEPT_FLUIDS = getHolder("triggers/action_station_accept_fluids");
        ACTION_STATION_ACCEPT_ITEMS = getHolder("triggers/action_station_accept_items");
        ACTION_STATION_ROBOT_MANDATORY = getHolder("triggers/action_station_robot_mandatory");
        ACTION_STATION_ROBOT_FORBIDDEN = getHolder("triggers/action_station_robot_forbidden");
        ACTION_STATION_PROVIDE_FLUIDS = getHolder("triggers/action_station_provide_fluids");
        ACTION_STATION_PROVIDE_ITEMS = getHolder("triggers/action_station_provide_items");
        ACTION_STATION_MACHINE_REQUEST = getHolder("triggers/action_station_machine_request");
        ACTION_STATION_REQUEST_ITEMS = getHolder("triggers/action_station_request_items");
        ACTION_ROBOT_GOTO_STATION = getHolder("triggers/action_robot_goto_station");
        ACTION_ROBOT_RESERVED = getHolder("triggers/trigger_robot_reserved");
        ACTION_ROBOT_LINKED = getHolder("triggers/trigger_robot_linked");
        ACTION_ROBOT_IN_STATION = getHolder("triggers/trigger_robot_in_station");
    }

    private static SpriteHolderRegistry.SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftrobotics:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
