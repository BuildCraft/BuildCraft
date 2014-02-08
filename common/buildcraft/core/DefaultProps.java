/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

public class DefaultProps {

	public static final String MOD = "BuildCraft";

	public static final String DEPENDENCY_CORE = "required-after:BuildCraft|Core@" + Version.VERSION;
	public static final String DEPENDENCY_TRANSPORT = "required-after:BuildCraft|Transport@" + Version.VERSION;

	public static int WORLD_HEIGHT = 256;
	public static final String NET_CHANNEL_NAME = "BC";
	public static int NETWORK_UPDATE_RANGE = 128;
	public static int PIPE_CONTENTS_RENDER_DIST = 24;

	public static String TEXTURE_PATH_GUI = "textures/gui";
	public static String TEXTURE_PATH_BLOCKS = "buildcraft:textures/blocks";
	public static String TEXTURE_PATH_ENTITIES = "textures/entities";

	public static final String DEFAULT_LANGUAGE = "en_US";

	public static String PUMP_DIMENSION_LIST = "+/*/*,+/-1/lava";

	public static int WOODEN_GEAR_ID = 19100;
	public static int STONE_GEAR_ID = 19101;
	public static int IRON_GEAR_ID = 19102;
	public static int GOLDEN_GEAR_ID = 19103;
	public static int DIAMOND_GEAR_ID = 19104;
	public static int TEMPLATE_ITEM_ID = 19105;
	public static int WRENCH_ID = 19106;
	public static int BUCKET_OIL_ID = 19107;
	public static int REDSTONE_CRYSTAL_ID = 19108;
	public static int PIPE_WATERPROOF_ID = 19109;
	public static int BUCKET_FUEL_ID = 19110;
	public static int GATE_ID = 19111;
	public static int BUCKET_REDPLASMA_ID = 19112;
	public static int PIPE_WIRE = 19113;
	public static int REDSTONE_CHIPSET = 19117;
	public static int BLUEPRINT_ITEM_ID = 19118;

	// Moving to safer id range
	public static int GATE_AUTARCHIC_ID = 19140;
	public static int PIPE_FACADE_ID = 19141;
	public static int PIPE_PLUG_ID = 19142;
	public static int ROBOT_STATION_ID = 19143;

	public static int PIPE_ITEMS_WOOD_ID = 19160;
	public static int PIPE_ITEMS_COBBLESTONE_ID = 19161;
	public static int PIPE_ITEMS_STONE_ID = 19162;
	public static int PIPE_ITEMS_IRON_ID = 19163;
	public static int PIPE_ITEMS_GOLD_ID = 19164;
	public static int PIPE_ITEMS_DIAMOND_ID = 19165;
	public static int PIPE_ITEMS_OBSIDIAN_ID = 19166;
	public static int PIPE_ITEMS_EMERALD_ID = 19167;
	public static int PIPE_ITEMS_QUARTZ_ID = 19168;
	public static int PIPE_ITEMS_LAPIS_ID = 19169;
	public static int PIPE_ITEMS_DAIZULI_ID = 19170;
	public static int PIPE_ITEMS_EMZULI_ID = 19171;

	public static int PIPE_LIQUIDS_WOOD_ID = 19180;
	public static int PIPE_LIQUIDS_COBBLESTONE_ID = 19181;
	public static int PIPE_LIQUIDS_STONE_ID = 19182;
	public static int PIPE_LIQUIDS_IRON_ID = 19183;
	public static int PIPE_LIQUIDS_GOLD_ID = 19184;
	public static int PIPE_LIQUIDS_DIAMOND_ID = 19185;
	public static int PIPE_LIQUIDS_OBSIDIAN_ID = 19186;
	public static int PIPE_LIQUIDS_EMERALD_ID = 19187;
	public static int PIPE_LIQUIDS_QUARTZ_ID = 19188;
	public static int PIPE_LIQUIDS_LAPIS_ID = 19189;
	public static int PIPE_LIQUIDS_DIAZULI_ID = 19110;

	public static int PIPE_POWER_WOOD_ID = 19200;
	public static int PIPE_POWER_COBBLESTONE_ID = 19201;
	public static int PIPE_POWER_STONE_ID = 19202;
	public static int PIPE_POWER_IRON_ID = 19203;
	public static int PIPE_POWER_GOLD_ID = 19204;
	public static int PIPE_POWER_DIAMOND_ID = 19205;
	public static int PIPE_POWER_OBSIDIAN_ID = 19206;
	public static int PIPE_POWER_EMERALD_ID = 19207;
	public static int PIPE_POWER_QUARTZ_ID = 19208;
	public static int PIPE_POWER_LAPIS_ID = 19209;
	public static int PIPE_POWER_DIAZULI_ID = 19210;
	public static int PIPE_POWER_HEAT_ID = 19211;

	public static int PIPE_ITEMS_VOID_ID = 19220;
	public static int PIPE_LIQUIDS_VOID_ID = 19221;
	public static int PIPE_ITEMS_SANDSTONE_ID = 19222;
	public static int PIPE_LIQUIDS_SANDSTONE_ID = 19223;
	public static int PIPE_STRUCTURE_COBBLESTONE_ID = 19224;

	public static int ROBOT_BASE_ITEM_ID = 19300;
	public static int ROBOT_BUILDER_ITEM_ID = 19301;
	public static int ROBOT_PICKER_ITEM_ID = 19302;

	public static int MINING_WELL_ID = 1500;
	public static int DRILL_ID = 1501;
	public static int AUTO_WORKBENCH_ID = 1502;
	public static int QUARRY_ID = 1503;
	public static int MARKER_ID = 1504;
	public static int FILLER_ID = 1505;
	public static int BUILDER_ID = 1507;
	public static int ARCHITECT_ID = 1508;
	public static int FRAME_ID = 1509;
	public static int ENGINE_ID = 1510;
	public static int PUMP_ID = 1511;
	public static int TANK_ID = 1512;
	public static int GENERIC_PIPE_ID = 1513;
	public static int REFINERY_ID = 1514;
	public static int BLUEPRINT_LIBRARY_ID = 1515;
	public static int LASER_ID = 1516;
	public static int ASSEMBLY_TABLE_ID = 1517;
	public static int PATH_MARKER_ID = 1518;
	public static int HOPPER_ID = 1519;
	public static int SPRING_ID = 1522;
	public static int FILTERED_BUFFER_ID = 1523;
	public static int FLOOD_GATE_ID = 1524;

	public static int RECEIVER_ID = 1525;
	public static int EMITTER_ID = 1526;

	public static int OIL_ID = 1530;
	public static int FUEL_ID = 1531;
	public static int REDPLASMA_ID = 1532;

	public static int URBANIST_ID = 1533;

	public static boolean CURRENT_CONTINUOUS = false;
	public static double PIPES_DURABILITY = 0.25D;
	public static boolean FILLER_DESTROY = false;

	public static final int FILLER_LIFESPAN_TOUGH = 20;
	public static final int FILLER_LIFESPAN_NORMAL = 6000;

	public static int BIOME_OIL_OCEAN = 160;
	public static int BIOME_OIL_DESERT = 161;
}
