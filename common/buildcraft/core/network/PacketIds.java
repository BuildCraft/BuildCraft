/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

public final class PacketIds {

	public static final int TILE_UPDATE = 0;
	public static final int COMMAND = 1;
	public static final int PIPE_TRAVELER = 2;
	public static final int PIPE_LIQUID = 3;
	public static final int PIPE_POWER = 4;
	public static final int PIPE_ITEMSTACK_REQUEST = 5;
	public static final int PIPE_ITEMSTACK = 6;
	public static final int ENTITY_UPDATE = 7;

	public static final int DIAMOND_PIPE_SELECT = 31;
	public static final int EMERALD_PIPE_SELECT = 32;

	public static final int TABLET_MESSAGE = 40;

	public static final int ADVANCED_WORKBENCH_SETSLOT = 70;

	public static final int GUI_RETURN = 80;
	public static final int GUI_WIDGET = 81;

	public static final int STATE_UPDATE = 100;

	/**
	 * Deactivate constructor
	 */
	private PacketIds() {
	}

}
