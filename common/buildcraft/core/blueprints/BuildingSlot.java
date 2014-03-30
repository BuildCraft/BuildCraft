/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import buildcraft.api.blueprints.IBuilderContext;
import net.minecraft.item.ItemStack;

public class BuildingSlot {

	public void writeToWorld(IBuilderContext context) {

	}

	public void postProcessing (IBuilderContext context) {

	}

	public LinkedList<ItemStack> getRequirements (IBuilderContext context) {
		return new LinkedList<ItemStack>();
	}
}
