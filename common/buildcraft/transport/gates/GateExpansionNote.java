/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import net.minecraft.tileentity.TileEntity;

import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IGateExpansion;

public final class GateExpansionNote extends GateExpansionBuildcraft implements IGateExpansion {

	public static GateExpansionNote INSTANCE = new GateExpansionNote();

	private GateExpansionNote() {
		super("note");
	}

	@Override
	public GateExpansionController makeController(TileEntity pipeTile) {
		return new GateExpansionControllerNote(pipeTile);
	}

	private class GateExpansionControllerNote extends GateExpansionController {

		public GateExpansionControllerNote(TileEntity pipeTile) {
			super(GateExpansionNote.this, pipeTile);
		}
	}
}
