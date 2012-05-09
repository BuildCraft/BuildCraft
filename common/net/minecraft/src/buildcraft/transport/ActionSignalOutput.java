/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.IPipe;

public class ActionSignalOutput extends Action {

	public IPipe.WireColor color;

	public ActionSignalOutput (int id, IPipe.WireColor color) {
		super (id);

		this.color = color;
	}

	@Override
	public int getIndexInTexture () {
		switch (color) {
		case Red:
			return 0 * 16 + 3;
		case Blue:
			return 0 * 16 + 5;
		case Green:
			return 0 * 16 + 7;
		case Yellow:
			return 0 * 16 + 9;
		}

		return 0;
	}

	@Override
	public String getDescription () {
		switch (color) {
		case Red:
			return "Red Pipe Signal";
		case Blue:
			return "Blue Pipe Signal";
		case Green:
			return "Green Pipe Signal";
		case Yellow:
			return "Yellow Pipe Signal";
		}

		return "";
	}

	@Override
	public String getTexture() {
		return BuildCraftCore.triggerTextures;
	}
}
