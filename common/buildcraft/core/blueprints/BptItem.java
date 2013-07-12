/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.blueprints;

import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import java.util.LinkedList;
import net.minecraft.item.ItemStack;

public class BptItem {

	public void addRequirements(BptSlotInfo slot, LinkedList<ItemStack> requirements) {

	}

	public void rotateLeft(BptSlotInfo slot, IBptContext context) {

	}

	public void buildBlock(BptSlotInfo slot, IBptContext context) {

	}

	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {

	}

	public void postProcessing(BptSlotInfo slot, IBptContext context) {

	}

}
