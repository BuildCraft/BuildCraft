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
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BuildCraftAPI;
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
			return getSchematic().getRequirements(context);
		}
	}

	@Override
	public Position getDestination () {
		return new Position (x + 0.5, y + 0.5, z + 0.5);
	}

	@Override
	public void writeCompleted (IBuilderContext context, double complete) {
		if (BuildCraftAPI.isSoftBlock(context.world(), x, y, z)) {
			getSchematic().writeCompleted(context, x, y, z, complete);
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

		NBTTagCompound schematicNBT = new NBTTagCompound();
		SchematicFactory.getFactory(schematic.getClass())
				.saveSchematicToWorldNBT(schematicNBT, schematic, registry);
		nbt.setTag("schematic", schematicNBT);
	}

	@Override
	public void readFromNBT (NBTTagCompound nbt, MappingRegistry registry) {
		mode = Mode.values() [nbt.getByte("mode")];
		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");

		schematic = (SchematicBlockBase) SchematicFactory
				.createSchematicFromWorldNBT(nbt.getCompoundTag("schematic"), registry);
	}

	@Override
	public LinkedList<ItemStack> getStacksToDisplay() {
		if (mode == Mode.ClearIfInvalid) {
			return stackConsumed;
		} else {
			return getSchematic ().getStacksToDisplay (stackConsumed);
		}
	}
}
