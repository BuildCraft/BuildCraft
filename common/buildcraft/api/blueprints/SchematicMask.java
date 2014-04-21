/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.LinkedList;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;

public class SchematicMask extends SchematicBlockBase {

	public boolean isConcrete = true;

	public SchematicMask () {

	}

	public SchematicMask (boolean isConcrete) {
		this.isConcrete = isConcrete;
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList <ItemStack> stacks) {
		if (isConcrete) {
			if (stacks.size() == 0 || !BlockUtil.isSoftBlock(context.world(), x, y, z)) {
				return;
			} else {
				ItemStack stack = stacks.getFirst();

				// force the block to be air block, in case it's just a soft
				// block which replacement is not straightforward
				context.world().setBlock(x, y, z, Blocks.air, 0, 3);

				stack.tryPlaceItemIntoWorld(
						CoreProxy.proxy.getBuildCraftPlayer(context.world()),
						context.world(), x, y, z, 1, 0.0f, 0.0f, 0.0f);
			}
		} else {
			context.world().setBlock(x, y, z, Blocks.air, 0, 3);
		}
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		if (isConcrete) {
			return !BlockUtil.isSoftBlock(context.world(), x, y, z);
		} else {
			return BlockUtil.isSoftBlock(context.world(), x, y, z);
		}
	}

	// TODO: To be removed with the "real" list of items
	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(Blocks.brick_block));
	}

	@Override
	public void writeCompleted(IBuilderContext context, int x, int y, int z, double completed) {
		if (!isConcrete) {
			context.world().destroyBlockInWorldPartially(0, x, y, z,
					(int) (completed * 10.0F) - 1);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		nbt.setBoolean("isConcrete", isConcrete);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		isConcrete = nbt.getBoolean("isConcrete");
	}
}
