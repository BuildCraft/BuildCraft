/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicBed extends SchematicBlock {

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if ((meta & 8) == 0) {
			requirements.add(new ItemStack(Items.bed));
		}
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		// cancel requirements reading
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		int orientation = meta & 7;
		int others = meta - orientation;

		switch (orientation) {
			case 0:
				meta = 1 + others;
				break;
			case 1:
				meta = 2 + others;
				break;
			case 2:
				meta = 3 + others;
				break;
			case 3:
				meta = 0 + others;
				break;
		}
	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		if ((meta & 8) != 0) {
			return;
		}

		context.world().setBlock(x, y, z, block, meta, 3);

		int x2 = x;
		int z2 = z;

		switch (meta) {
			case 0:
				z2++;
				break;
			case 1:
				x2--;
				break;
			case 2:
				z2--;
				break;
			case 3:
				x2++;
				break;
		}

		context.world().setBlock(x2, y, z2, block, meta + 8, 3);
	}

	@Override
	public boolean doNotBuild() {
		return (meta & 8) != 0;
	}
}
