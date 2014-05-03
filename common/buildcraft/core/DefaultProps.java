/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

public final class DefaultProps {

	public static final String MOD = "BuildCraft";

	public static final String DEPENDENCY_CORE = "required-after:BuildCraft|Core@" + Version.VERSION;
	public static final String DEPENDENCY_TRANSPORT = "required-after:BuildCraft|Transport@" + Version.VERSION;

	public static final String NET_CHANNEL_NAME = "BC";
	public static int NETWORK_UPDATE_RANGE = 128;
	public static int PIPE_CONTENTS_RENDER_DIST = 24;

	public static String TEXTURE_PATH_GUI = "textures/gui";
	public static String TEXTURE_PATH_BLOCKS = "buildcraft:textures/blocks";
	public static String TEXTURE_PATH_ENTITIES = "textures/entities";

	public static final String DEFAULT_LANGUAGE = "en_US";

	public static String PUMP_DIMENSION_LIST = "+/*/*,+/-1/lava";

	public static boolean CURRENT_CONTINUOUS = false;
	public static double PIPES_DURABILITY = 0.25D;
	public static boolean FILLER_DESTROY = false;

	public static final int FILLER_LIFESPAN_TOUGH = 20;
	public static final int FILLER_LIFESPAN_NORMAL = 6000;

	public static int BIOME_OIL_OCEAN = 126;
	public static int BIOME_OIL_DESERT = 127;

	/**
	 * Deactivate constructor
	 */
	private DefaultProps() {
	}
}
