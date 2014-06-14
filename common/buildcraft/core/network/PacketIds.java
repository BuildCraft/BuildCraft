/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

public final class PacketIds {

	public static final int TILE_UPDATE = 0;
	// public static final int PIPE_DESCRIPTION = 1;
	public static final int PIPE_TRAVELER = 2;
	public static final int PIPE_LIQUID = 3;
	public static final int PIPE_POWER = 4;
	public static final int PIPE_ITEMSTACK_REQUEST = 5;
	public static final int PIPE_ITEMSTACK = 6;
	public static final int PIPE_GATE_EXPANSION_MAP = 7;

	public static final int DIAMOND_PIPE_SELECT = 31;
	public static final int EMERALD_PIPE_SELECT = 32;

	public static final int REFINERY_FILTER_SET = 50;

	public static final int ADVANCED_WORKBENCH_SETSLOT = 70;
	public static final int SELECTION_ADVANCED_WORKBENCH = 71;

	public static final int GUI_RETURN = 80;
	public static final int GUI_WIDGET = 81;

	public static final int STATE_UPDATE = 100;

	public static final int RPC_TILE = 110;
	public static final int RPC_PIPE = 111;
	public static final int RPC_GUI = 112;
	public static final int RPC_ENTITY = 113;

	/**
	 * Deactivate constructor
	 */
	private PacketIds() {
	}

}
