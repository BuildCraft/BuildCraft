/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

public class DefaultProps {

	public static final String VERSION = "3.x SVN r2";
	
	public static int WORLD_HEIGHT       = 256;
	public static String NET_CHANNEL_NAME = "BC";
	public static int NETWORK_UPDATE_RANGE = 128;

	public static String TEXTURE_ICONS = "/net/minecraft/src/buildcraft/core/gui/icons.png";
	
	public static int WOODEN_GEAR_ID     = 3800;
	public static int STONE_GEAR_ID      = 3801;
	public static int IRON_GEAR_ID       = 3802;
	public static int GOLDEN_GEAR_ID     = 3803;
	public static int DIAMOND_GEAR_ID    = 3804;
	public static int TEMPLATE_ITEM_ID   = 3805;
	public static int WRENCH_ID          = 3806;
	public static int BUCKET_OIL_ID      = 3807;
	public static int FUEL_ID            = 3808;
	public static int PIPE_WATERPROOF_ID = 3809;
	public static int BUCKET_FUEL_ID     = 3810;
	public static int GATE_ID            = 3811;
	public static int RED_PIPE_WIRE      = 3813;
	public static int BLUE_PIPE_WIRE     = 3814;
	public static int GREEN_PIPE_WIRE    = 3815;
	public static int YELLOW_PIPE_WIRE   = 3816;
	public static int REDSTONE_CHIPSET   = 3817;
	public static int BLUEPRINT_ITEM_ID  = 3818;

	// Moving to safer id range
	public static int GATE_AUTARCHIC_ID	= 19000;

	public static int PIPE_ITEMS_WOOD_ID          = 4050;
	public static int PIPE_ITEMS_COBBLESTONE_ID   = 4051;
	public static int PIPE_ITEMS_STONE_ID         = 4052;
	public static int PIPE_ITEMS_IRON_ID          = 4053;
	public static int PIPE_ITEMS_GOLD_ID          = 4054;
	public static int PIPE_ITEMS_DIAMOND_ID       = 4055;
	public static int PIPE_ITEMS_OBSIDIAN_ID      = 4056;

	public static int PIPE_LIQUIDS_WOOD_ID        = 4057;
	public static int PIPE_LIQUIDS_COBBLESTONE_ID = 4058;
	public static int PIPE_LIQUIDS_STONE_ID       = 4059;
	public static int PIPE_LIQUIDS_IRON_ID        = 4060;
	public static int PIPE_LIQUIDS_GOLD_ID        = 4061;
	public static int PIPE_LIQUIDS_DIAMOND_ID     = 4062;
	public static int PIPE_LIQUIDS_OBSIDIAN_ID    = 4063;

	public static int PIPE_POWER_WOOD_ID          = 4064;
	public static int PIPE_POWER_COBBLESTONE_ID   = 4065;
	public static int PIPE_POWER_STONE_ID         = 4066;
	public static int PIPE_POWER_IRON_ID          = 4067;
	public static int PIPE_POWER_GOLD_ID          = 4068;
	public static int PIPE_POWER_DIAMOND_ID       = 4069;
	public static int PIPE_POWER_OBSIDIAN_ID      = 4070;

	public static int PIPE_ITEMS_STRIPES_ID       = 4071;
	public static int PIPE_STRUCTURE_COBBLESTONE_ID  = 4072;

	public static int WOODEN_PIPE_ID       = 145; // LEGACY
	public static int STONE_PIPE_ID        = 146; // LEGACY
    public static int IRON_PIPE_ID         = 147; // LEGACY
    public static int GOLDEN_PIPE_ID       = 148; // LEGACY
    public static int DIAMOND_PIPE_ID      = 149; // LEGACY
    public static int MINING_WELL_ID       = 150;
    public static int DRILL_ID             = 151;
    public static int AUTO_WORKBENCH_ID    = 152;
    public static int QUARRY_ID            = 153;
    public static int MARKER_ID            = 154;
    public static int FILLER_ID            = 155;
    public static int OBSIDIAN_PIPE_ID     = 156; // LEGACY
    public static int BUILDER_ID           = 157;
    public static int ARCHITECT_ID          = 158;
    public static int COBBLESTONE_PIPE_ID  = 159; // LEGACY
    public static int FRAME_ID             = 160;
    public static int ENGINE_ID            = 161;
    public static int OIL_MOVING_ID        = 162;
    public static int OIL_STILL_ID         = 163;
    public static int PUMP_ID              = 164;
    public static int TANK_ID              = 165;
    public static int GENERIC_PIPE_ID      = 166;
    public static int REFINERY_ID          = 167;
	public static int BLUEPRINT_LIBRARY_ID = 168;
	public static int LASER_ID             = 169;
	public static int ASSEMBLY_TABLE_ID    = 170;
	public static int PATH_MARKER_ID       = 171;
	public static int HOPPER_ID 		   = 172;

	public static boolean CURRENT_CONTINUOUS   = false;
	public static boolean PIPES_ALWAYS_CONNECT = false;

	public static int TRIGGER_REDSTONE_ACTIVE        = 1;
	public static int TRIGGER_REDSTONE_INACTIVE      = 2;
	public static int TRIGGER_MACHINE_ACTIVE         = 3;
	public static int TRIGGER_MACHINE_INACTIVE       = 4;
	public static int TRIGGER_EMPTY_INVENTORY        = 5;
	public static int TRIGGER_CONTAINS_INVENTORY     = 6;
	public static int TRIGGER_SPACE_INVENTORY        = 7;
	public static int TRIGGER_FULL_INVENTORY         = 8;
	public static int TRIGGER_EMPTY_LIQUID           = 9;
	public static int TRIGGER_CONTAINS_LIQUID        = 10;
	public static int TRIGGER_SPACE_LIQUID           = 11;
	public static int TRIGGER_FULL_LIQUID            = 12;
	public static int TRIGGER_PIPE_EMPTY             = 13;
	public static int TRIGGER_PIPE_ITEMS             = 14;
	public static int TRIGGER_PIPE_LIQUIDS           = 15;
	public static int TRIGGER_PIPE_ENERGY            = 16;
	public static int TRIGGER_RED_SIGNAL_ACTIVE      = 17;
	public static int TRIGGER_RED_SIGNAL_INACTIVE    = 18;
	public static int TRIGGER_BLUE_SIGNAL_ACTIVE     = 19;
	public static int TRIGGER_BLUE_SIGNAL_INACTIVE   = 20;
	public static int TRIGGER_GREEN_SIGNAL_ACTIVE    = 21;
	public static int TRIGGER_GREEN_SIGNAL_INACTIVE  = 22;
	public static int TRIGGER_YELLOW_SIGNAL_ACTIVE   = 23;
	public static int TRIGGER_YELLOW_SIGNAL_INACTIVE = 24;
	public static int TRIGGER_BLUE_ENGINE_HEAT       = 25;
	public static int TRIGGER_GREEN_ENGINE_HEAT      = 26;
	public static int TRIGGER_YELLOW_ENGINE_HEAT     = 27;
	public static int TRIGGER_RED_ENGINE_HEAT        = 28;

	public static int ACTION_REDSTONE      = 1;
	public static int ACTION_RED_SIGNAL    = 2;
	public static int ACTION_BLUE_SIGNAL   = 3;
	public static int ACTION_GREEN_SIGNAL  = 4;
	public static int ACTION_YELLOW_SIGNAL = 5;
	public static int ACTION_ON            = 6;
	public static int ACTION_OFF           = 7;
	public static int ACTION_LOOP          = 8;
	public static int ACTION_ENERGY_PULSER = 9;
}

