/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.blueprints;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe.SideProperties;

public class SchematicPipe extends SchematicTile {

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
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

		NBTTagCompound gateNBT = cpt.getCompoundTag("Gate");

		for (int i = 0; i < 8; ++i) {
			if (gateNBT.hasKey("trigger[" + i + "]")) {
				ITrigger t = ActionManager.triggers.get(gateNBT.getString("trigger[" + i + "]"));
				t = t.rotateLeft ();
				gateNBT.setString("trigger[" + i + "]", t.getUniqueTag());
			}

			if (gateNBT.hasKey("action[" + i + "]")) {
				IAction a = ActionManager.actions.get(gateNBT.getString("action[" + i + "]"));
				a = a.rotateLeft ();
				gateNBT.setString("action[" + i + "]", a.getUniqueTag());
			}
		}
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList <ItemStack> stacks) {
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
			ArrayList <ItemStack> items = pipe.computeItemDrop ();
			storedRequirements = new ItemStack [items.size() + 1];
			items.toArray(storedRequirements);
			storedRequirements[storedRequirements.length - 1] = new ItemStack(
					pipe.item);

			tile.writeToNBT(cpt);

			// This overrides the default pipeId

			cpt.setInteger("pipeId", context.getMappingRegistry()
					.getIdForItem(pipe.item));

			// remove all pipe contents

			cpt.removeTag("travelingEntities");

			for (ForgeDirection direction : ForgeDirection.values()) {
				cpt.removeTag("tank[" + direction.ordinal() + "]");
				cpt.removeTag("transferState[" + direction.ordinal() + "]");
			}

			for (int i = 0; i < 6; ++i) {
				cpt.removeTag("powerQuery[" + i + "]");
				cpt.removeTag("nextPowerQuery[" + i + "]");
				cpt.removeTag("internalPower[" + i + "]");
				cpt.removeTag("internalNextPower[" + i + "]");
			}
		}
	}

	@Override
	public void postProcessing(IBuilderContext context, int x, int y, int z) {
		Item pipeItem = context.getMappingRegistry().getItemForId(cpt.getInteger("pipeId"));

		if (BptPipeExtension.contains(pipeItem)) {
			BptPipeExtension.get(pipeItem).postProcessing(this, context);
		}
	}
}
