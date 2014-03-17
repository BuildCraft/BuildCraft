/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicRedstoneDiode extends SchematicBlock {

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(Items.repeater));
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		int step = meta - (meta & 3);

		switch (meta - step) {
		case 0:
			meta = 1 + step;
			break;
		case 1:
			meta = 2 + step;
			break;
		case 2:
			meta = 3 + step;
			break;
		case 3:
			meta = 0 + step;
			break;
		}
	}
}
