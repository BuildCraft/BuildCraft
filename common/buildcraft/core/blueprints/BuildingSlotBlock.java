/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.Position;

public class BuildingSlotBlock extends BuildingSlot {

	public int x, y, z;
	public SchematicBlockBase schematic;

	public enum Mode {
		ClearIfInvalid, Build
	};

	public Mode mode = Mode.Build;

	public int buildStage = 0;

	@Override
	public SchematicBlockBase getSchematic () {
		if (schematic == null) {
			return new SchematicMask(false);
		} else {
			return schematic;
		}
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		if (mode == Mode.ClearIfInvalid) {
			if (!getSchematic().isAlreadyBuilt(context, x, y, z)) {
				context.world().setBlockToAir(x, y, z);
			}
		} else {
			try {
				getSchematic().writeToWorld(context, x, y, z, stackConsumed);

				// Once the schematic has been written, we're going to issue
				// calls
				// to various functions, in particular updating the tile entity.
				// If these calls issue problems, in order to avoid corrupting
				// the world, we're logging the problem and setting the block to
				// air.

				TileEntity e = context.world().getTileEntity(x, y, z);

				if (e != null) {
					e.updateEntity();
				}
			} catch (Throwable t) {
				t.printStackTrace();
				context.world().setBlockToAir(x, y, z);
			}
		}
	}

	@Override
	public void postProcessing (IBuilderContext context) {
		getSchematic().postProcessing(context, x, y, z);
	}

	@Override
	public LinkedList<ItemStack> getRequirements (IBuilderContext context) {
		if (mode == Mode.ClearIfInvalid) {
			return new LinkedList<ItemStack>();
		} else {
			LinkedList<ItemStack> req = new LinkedList<ItemStack>();

			getSchematic().writeRequirementsToWorld(context, req);

			return req;
		}
	}

	@Override
	public Position getDestination () {
		return new Position (x + 0.5, y + 0.5, z + 0.5);
	}

	@Override
	public void writeCompleted (IBuilderContext context, double complete) {
		if (mode == Mode.ClearIfInvalid) {
			context.world().destroyBlockInWorldPartially(0, x, y, z,
					(int) (complete * 10.0F) - 1);
		}
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context) {
		return schematic.isAlreadyBuilt(context, x, y, z);
	}

	@Override
	public void writeToNBT (NBTTagCompound nbt, MappingRegistry registry) {
		nbt.setByte("mode", (byte) mode.ordinal());
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);

		if (schematic != null) {
			NBTTagCompound schematicNBT = new NBTTagCompound();
			SchematicFactory.getFactory(schematic.getClass())
					.saveSchematicToWorldNBT(schematicNBT, schematic, registry);
			nbt.setTag("schematic", schematicNBT);
		}

		NBTTagList nbtStacks = new NBTTagList ();

		for (ItemStack stack  : stackConsumed) {
			NBTTagCompound nbtStack = new NBTTagCompound();
			stack.writeToNBT(nbtStack);
			nbtStacks.appendTag(nbtStack);
		}

		nbt.setTag("stackConsumed", nbtStacks);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) throws MappingNotFoundException {
		mode = Mode.values() [nbt.getByte("mode")];
		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");

		if (nbt.hasKey("schematic")) {
			schematic = (SchematicBlockBase) SchematicFactory
				.createSchematicFromWorldNBT(nbt.getCompoundTag("schematic"), registry);
		}

		stackConsumed = new LinkedList<ItemStack>();

		NBTTagList nbtStacks = nbt.getTagList("stackConsumed", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < nbtStacks.tagCount(); ++i) {
			stackConsumed.add(ItemStack.loadItemStackFromNBT(nbtStacks
					.getCompoundTagAt(i)));
		}

	}

	@Override
	public LinkedList<ItemStack> getStacksToDisplay() {
		if (mode == Mode.ClearIfInvalid) {
			return stackConsumed;
		} else {
			return getSchematic ().getStacksToDisplay (stackConsumed);
		}
	}

	@Override
	public double getEnergyRequirement() {
		return schematic.getEnergyRequirement(stackConsumed);
	}
}
