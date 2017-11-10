/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

public final class DefaultProps {

	public static final String MOD = "BuildCraft";

	public static final String DEPENDENCY_CORE = "required-after:BuildCraft|Core@" + Version.VERSION;

	public static final String NET_CHANNEL_NAME = "BC";
	public static final int MAX_NAME_SIZE = 32;
	public static int NETWORK_UPDATE_RANGE = 64;
	public static int MARKER_RANGE = 64;
	public static int PIPE_CONTENTS_RENDER_DIST = 24;

	public static String TEXTURE_PATH_ROBOTS = "buildcraftrobotics:textures/entities";

	public static String PUMP_DIMENSION_LIST = "+/*/*,+/-1/lava";

	public static double PIPES_DURABILITY = 0.25D;
	public static int PIPES_FLUIDS_BASE_FLOW_RATE = 10;

	public static int BIOME_OIL_OCEAN = 126;
	public static int BIOME_OIL_DESERT = 127;

	/**
	 * Deactivate constructor
	 */
	private DefaultProps() {
	}
}
