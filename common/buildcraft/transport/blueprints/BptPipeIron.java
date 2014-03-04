/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.blueprints;

import net.minecraft.item.Item;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.IBptContext;

public class BptPipeIron extends BptPipeExtension {

	public BptPipeIron(Item i) {
		super(i);
	}

	@Override
	public void rotateLeft(BptBlock slot, IBptContext context) {
		int orientation = slot.meta & 7;
		int others = slot.meta - orientation;

		slot.meta = ForgeDirection.values()[orientation].getRotation(ForgeDirection.UP).ordinal() + others;
	}

}
