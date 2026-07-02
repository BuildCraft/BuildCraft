package ct.buildcraft.robotics;

import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BCRoboticsSprites {

    // ── Triggers ──────────────────────────────────────────────
    public static final SpriteHolder TRIGGER_ROBOT_IN_STATION;
    public static final SpriteHolder TRIGGER_ROBOT_SLEEP;
    public static final SpriteHolder TRIGGER_ROBOT_LINKED;
    public static final SpriteHolder TRIGGER_ROBOT_RESERVED;

    // ── Robot actions ─────────────────────────────────────────
    public static final SpriteHolder ACTION_ROBOT_FILTER;
    public static final SpriteHolder ACTION_ROBOT_FILTER_TOOL;
    public static final SpriteHolder ACTION_ROBOT_GOTO_STATION;
    public static final SpriteHolder ACTION_ROBOT_WAKEUP;
    public static final SpriteHolder ACTION_ROBOT_WORK_IN_AREA;
    public static final SpriteHolder ACTION_ROBOT_LOAD_UNLOAD_AREA;

    // ── Station actions ───────────────────────────────────────
    public static final SpriteHolder ACTION_STATION_ACCEPT_FLUIDS;
    public static final SpriteHolder ACTION_STATION_ACCEPT_ITEMS;
    public static final SpriteHolder ACTION_STATION_ROBOT_FORBIDDEN;
    public static final SpriteHolder ACTION_STATION_ROBOT_MANDATORY;
    public static final SpriteHolder ACTION_STATION_PROVIDE_FLUIDS;
    public static final SpriteHolder ACTION_STATION_PROVIDE_ITEMS;
    public static final SpriteHolder ACTION_STATION_REQUEST_ITEMS;
    public static final SpriteHolder ACTION_STATION_MACHINE_REQUEST;

    static {
        TRIGGER_ROBOT_IN_STATION   = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/trigger_robot_in_station");
        TRIGGER_ROBOT_SLEEP        = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/trigger_robot_sleep");
        TRIGGER_ROBOT_LINKED       = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/trigger_robot_linked");
        TRIGGER_ROBOT_RESERVED     = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/trigger_robot_reserved");

        ACTION_ROBOT_FILTER        = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_robot_filter");
        ACTION_ROBOT_FILTER_TOOL   = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_robot_filter_tool");
        ACTION_ROBOT_GOTO_STATION  = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_robot_goto_station");
        ACTION_ROBOT_WAKEUP        = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_robot_wakeup");
        ACTION_ROBOT_WORK_IN_AREA  = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_robot_work_in_area");
        ACTION_ROBOT_LOAD_UNLOAD_AREA = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_robot_load_unload_area");

        ACTION_STATION_ACCEPT_FLUIDS  = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_accept_fluids");
        ACTION_STATION_ACCEPT_ITEMS   = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_accept_items");
        ACTION_STATION_ROBOT_FORBIDDEN = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_robot_forbidden");
        ACTION_STATION_ROBOT_MANDATORY = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_robot_mandatory");
        ACTION_STATION_PROVIDE_FLUIDS = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_provide_fluids");
        ACTION_STATION_PROVIDE_ITEMS  = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_provide_items");
        ACTION_STATION_REQUEST_ITEMS  = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_request_items");
        ACTION_STATION_MACHINE_REQUEST = SpriteHolderRegistry.getHolder("buildcraftrobotics:triggers/action_station_machine_request");
    }

    public static void preInit() {}
}
