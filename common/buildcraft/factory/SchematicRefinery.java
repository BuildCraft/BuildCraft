/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicRefinery extends SchematicTile {

	@Override
	public void rotateLeft(IBuilderContext context) {
		meta = ForgeDirection.values()[meta].getRotation(ForgeDirection.UP).ordinal();
	}

	@Override
	public void writeToBlueprint(IBuilderContext context, int x, int y, int z) {
		TileRefinery refinery = (TileRefinery) context.world().getTileEntity(x, y, z);

//		slot.cpt.setInteger("filter0", refinery.getFilter(0));
//		slot.cpt.setInteger("filter1", refinery.getFilter(1));
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		super.writeToWorld(context, x, y, z, stacks);

		TileRefinery refinery = (TileRefinery) context.world().getTileEntity(x, y, z);

		int filter0 = tileNBT.getInteger("filter0");
		int filter1 = tileNBT.getInteger("filter1");
		int filterMeta0 = 0;
		int filterMeta1 = 0;

		if (tileNBT.hasKey("filterMeta0")) {
			filterMeta0 = tileNBT.getInteger("filterMeta0");
		}
		if (tileNBT.hasKey("filterMeta1")) {
			filterMeta1 = tileNBT.getInteger("filterMeta1");
		}

//		refinery.setFilter(0, filter0, filterMeta0);
//		refinery.setFilter(1, filter1, filterMeta1);
	}

}
