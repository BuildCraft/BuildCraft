/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

public enum PacketIds {

	// General packets

	TileDescription, TileUpdate,

	// Transport packets

	PipeItem, DiamondPipeContents, AssemblyTableSelect, AssemblyTableGetSelection, DiamondPipeGUI, AssemblyTableGUI,

	// Factory packets

	AutoCraftingGUI,

	// Builders packets

	FillerGUI, TemplateGUI, BuilderGUI,

	// Energy packets

	EngineSteamGUI, EngineCombustionGUI

}
