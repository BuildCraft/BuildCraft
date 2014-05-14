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

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidContainerRegistry;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicFluid extends SchematicBlock {

	private final ItemStack bucketStack;

	public SchematicFluid(ItemStack bucketStack) {
		this.bucketStack = bucketStack;
	}

	@Override
	public void writeRequirementsToWorld(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if (meta == 0) {
			requirements.add(bucketStack.copy());
		}
	}

	@Override
	public void writeRequirementsToBlueprint(IBuilderContext context, int x, int y, int z) {
		// cancel requirements reading
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		if (meta == 0) {
			return block == context.world().getBlock(x, y, z) && context.world().getBlockMetadata(x, y, z) == 0;
		} else {
			return block == context.world().getBlock(x, y, z);
		}
	}

	@Override
	public void rotateLeft(IBuilderContext context) {

	}

	@Override
	public boolean doNotBuild() {
		return meta != 0;
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		if (meta == 0) {
			context.world().setBlock(x, y, z, block, 0, 3);
		}
	}

	@Override
	public void postProcessing(IBuilderContext context, int x, int y, int z) {
		if (meta != 0) {
			context.world().setBlock(x, y, z, block, meta, 3);
		}
	}

	@Override
	public LinkedList<ItemStack> getStacksToDisplay(
			LinkedList<ItemStack> stackConsumed) {

		ItemStack s = new ItemStack(FluidContainerRegistry
				.getFluidForFilledItem(bucketStack).getFluid().getBlock());
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();

		result.add(s);

		return result;
	}

}
