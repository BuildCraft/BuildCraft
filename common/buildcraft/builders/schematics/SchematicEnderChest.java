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

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;

public class SchematicEnderChest extends SchematicRotateMeta {

	public SchematicEnderChest() {
		super (new int[]{2, 5, 3, 4}, true);
	}

	@Override
	public void writeRequirementsToWorld(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(Blocks.obsidian, 8));
		requirements.add(new ItemStack(Items.ender_eye, 1));
	}

	@Override
	public void writeRequirementsToBlueprint(IBuilderContext context, int x, int y, int z) {
		// cancel requirements reading
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return block == context.world().getBlock(x, y, z);
	}

}
