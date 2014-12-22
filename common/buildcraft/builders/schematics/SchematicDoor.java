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

import net.minecraft.block.BlockDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.core.BlockBuildCraft;

public class SchematicDoor extends SchematicBlock {

	final ItemStack stack;

	int upperMeta = 0;

	public SchematicDoor(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if ((getMetaData() & 8) == 0) {
			requirements.add(stack.copy());
		}
	}

	@Override
	public void storeRequirements(IBuilderContext context, BlockPos pos) {
		// cancel requirements reading
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		state = state.withProperty(BlockBuildCraft.FACING_PROP, EnumFacing.getFront(rotateMeta(this.getMetaData())));
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
		return (getMetaData() & 8) != 0;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
		return state.getBlock() == context.world().getBlockState(pos).getBlock();
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {

		context.world().setBlockState(pos, state.withProperty(BlockDoor.FACING, getFace()), 3);
		context.world().setBlockState(pos.up(), state.withProperty(BlockDoor.FACING, EnumFacing.getFront(upperMeta)), 3);
	}

	@Override
	public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
		super.initializeFromObjectAt(context, pos);

		if ((getMetaData() & 8) == 0) {
			upperMeta = ((EnumFacing)context.world().getBlockState(pos.up()).getValue(BlockBuildCraft.FACING_PROP)).getIndex();;
		}
	}

	@Override
	public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		super.writeSchematicToNBT(nbt, registry);

		nbt.setByte("upperMeta", (byte) upperMeta);
	}

	@Override
	public void readSchematicFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		super.readSchematicFromNBT(nbt, registry);

		upperMeta = nbt.getByte("upperMeta");
	}
}
