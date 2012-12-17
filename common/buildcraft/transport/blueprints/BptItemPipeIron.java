/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.blueprints;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.core.blueprints.BptItem;

public class BptItemPipeIron extends BptItem {

	public BptItemPipeIron() {

	}

	@Override
	public void addRequirements(BptSlotInfo slot, LinkedList<ItemStack> requirements) {

	}

	@Override
	public void postProcessing(BptSlotInfo slot, IBptContext context) {
		context.world().setBlockMetadata(slot.x, slot.y, slot.z, slot.meta);
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		int orientation = slot.meta & 7;
		int others = slot.meta - orientation;

		slot.meta = ForgeDirection.values()[orientation].getRotation(ForgeDirection.DOWN).ordinal() + others;
	}

}
