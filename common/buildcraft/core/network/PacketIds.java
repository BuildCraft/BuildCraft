package buildcraft.core.network;

public class PacketIds {

	public static final int TILE_UPDATE = 0;
	// public static final int PIPE_DESCRIPTION = 1;
	public static final int PIPE_CONTENTS = 2;
	public static final int PIPE_LIQUID = 3;
	public static final int PIPE_POWER = 4;
	public static final int REQUEST_ITEM_NBT = 5;
	public static final int PIPE_ITEM_NBT = 6;

	public static final int SELECTION_ASSEMBLY_GET = 20;
	/** Packet sent to server when a recipe is clicked on in the assembly table */
	public static final int SELECTION_ASSEMBLY = 21;
	/** Packet to send recipes to client */
	public static final int SELECTION_ASSEMBLY_SEND = 22;

	public static final int DIAMOND_PIPE_SELECT = 31;
	public static final int EMERALD_PIPE_SELECT = 32;

	public static final int GATE_ACTIONS = 40;
	public static final int GATE_REQUEST_INIT = 41;
	public static final int GATE_REQUEST_SELECTION = 42;
	public static final int GATE_SELECTION = 43;
	public static final int GATE_SELECTION_CHANGE = 44;
	public static final int GATE_TRIGGERS = 45;

	public static final int REFINERY_FILTER_SET = 50;

	public static final int ARCHITECT_NAME = 60;
	public static final int LIBRARY_ACTION = 61;
	public static final int LIBRARY_SELECT = 62;

	public static final int ADVANCED_WORKBENCH_SETSLOT = 70;
	public static final int SELECTION_ADVANCED_WORKBENCH = 71;

	public static final int GUI_RETURN = 80;
	public static final int GUI_WIDGET = 81;

	public static final int STATE_UPDATE = 100;
}
