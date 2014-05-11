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
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicDoor extends SchematicBlock {

	final ItemStack stack;

	int upperMeta = 0;

	public SchematicDoor(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void writeRequirementsToWorld(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if ((meta & 8) == 0) {
			requirements.add(stack.copy());
		}
	}

	@Override
	public void writeRequirementsToBlueprint(IBuilderContext context, int x, int y, int z) {
		// cancel requirements reading
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		meta = rotateMeta(meta);
		upperMeta = rotateMeta(upperMeta);
	}

	private int rotateMeta (int meta) {
		int orientation = meta & 3;
		int others = meta - orientation;

		switch (orientation) {
		case 0:
			return 1 + others;
		case 1:
			return 2 + others;
		case 2:
			return 3 + others;
		case 3:
			return 0 + others;
		}

		return 0;
	}

	@Override
	public boolean doNotBuild() {
		return (meta & 8) != 0;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return block == context.world().getBlock(x, y, z);
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		context.world().setBlock(x, y, z, block, meta, 3);
		context.world().setBlock(x, y + 1, z, block, upperMeta, 3);

		context.world().setBlockMetadataWithNotify(x, y + 1, z, upperMeta, 3);
		context.world().setBlockMetadataWithNotify(x, y, z, meta, 3);
	}

	@Override
	public void writeToBlueprint(IBuilderContext context, int x, int y, int z) {
		super.writeToBlueprint(context, x, y, z);

		if ((meta & 8) == 0) {
			upperMeta = context.world().getBlockMetadata(x, y + 1, z);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		super.writeToNBT(nbt, registry);

		nbt.setByte("upperMeta", (byte) upperMeta);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		super.readFromNBT(nbt, registry);

		upperMeta = nbt.getByte("upperMeta");
	}
}
