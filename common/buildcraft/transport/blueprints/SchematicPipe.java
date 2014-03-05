/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.blueprints;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe.SideProperties;

public class SchematicPipe extends Schematic {

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		Item pipeItem = context.getMappingRegistry().getItemForId (cpt.getInteger("pipeId"));

		requirements.add(new ItemStack(pipeItem));

		requirements.addAll(storedRequirements);
	}

	@Override
	public boolean isValid(IBuilderContext context) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), x, y, z);

		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.item == context.getMappingRegistry().getItemForId(
					cpt.getInteger("pipeId"));
		} else {
			return false;
		}
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		SideProperties props = new SideProperties ();

		props.readFromNBT(cpt);
		props.rotateLeft();
		props.writeToNBT(cpt);

		Item pipeItem = context.getMappingRegistry().getItemForId(cpt.getInteger("pipeId"));

		if (BptPipeExtension.contains(pipeItem)) {
			BptPipeExtension.get(pipeItem).rotateLeft(this, context);
		}
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		cpt.setInteger("x", x);
		cpt.setInteger("y", y);
		cpt.setInteger("z", z);
		cpt.setInteger(
				"pipeId",
				Item.getIdFromItem(context.getMappingRegistry().getItemForId(
						cpt.getInteger("pipeId"))));

		context.world().setBlock(x, y, z, block, meta, 3);

		TileEntity tile = context.world().getTileEntity(x, y, z);
		tile.readFromNBT(cpt);
	}

	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		TileEntity tile = context.world().getTileEntity(x, y, z);
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), x, y, z);

		if (BlockGenericPipe.isValid(pipe)) {
			storedRequirements.addAll(pipe.computeItemDrop ());

			tile.writeToNBT(cpt);

			// This overrides the default pipeId
			cpt.setInteger("pipeId", context.getMappingRegistry()
					.getIdForItem(pipe.item));
		}
	}

	@Override
	public void postProcessing(IBuilderContext context) {
		Item pipeItem = context.getMappingRegistry().getItemForId(cpt.getInteger("pipeId"));

		if (BptPipeExtension.contains(pipeItem)) {
			BptPipeExtension.get(pipeItem).postProcessing(this, context);
		}
	}
}
