/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicBlockBase;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.Position;
import buildcraft.core.blueprints.IndexRequirementMap;
import buildcraft.core.lib.utils.BlockUtils;

public class BuildingSlotBlock extends BuildingSlot {
	public int x, y, z;
	public SchematicBlockBase schematic;

	// TODO: Remove this ugly hack
	public IndexRequirementMap internalRequirementRemovalListener;

	public enum Mode {
		ClearIfInvalid, Build
	}

	public Mode mode = Mode.Build;

	public int buildStage = 0;

	@Override
	public SchematicBlockBase getSchematic() {
		if (schematic == null) {
			return new SchematicMask(false);
		} else {
			return schematic;
		}
	}

	@Override
	public boolean writeToWorld(IBuilderContext context) {
		if (internalRequirementRemovalListener != null) {
			internalRequirementRemovalListener.remove(this);
		}

		if (mode == Mode.ClearIfInvalid) {
			if (!getSchematic().isAlreadyBuilt(context, x, y, z)) {
				if (BuildCraftBuilders.dropBrokenBlocks) {
					return BlockUtils.breakBlock((WorldServer) context.world(), x, y, z);
				} else {
					context.world().setBlockToAir(x, y, z);
					return true;
				}
			}
		} else {
			try {
				getSchematic().placeInWorld(context, x, y, z, stackConsumed);

				// This is also slightly hackish, but that's what you get when
				// you're unable to break an API too much.
				if (!getSchematic().isAlreadyBuilt(context, x, y, z)) {
					if (context.world().isAirBlock(x, y, z)) {
						return false;
					} else if (!(getSchematic() instanceof SchematicBlock)
						|| context.world().getBlock(x, y, z).isAssociatedBlock(((SchematicBlock) getSchematic()).block)) {
						BCLog.logger.warn("Placed block does not match expectations! Most likely a bug in BuildCraft or a supported mod. Removed mismatched block.");
						BCLog.logger.warn("Location: " + x + ", " + y + ", " + z + " - Block: " + Block.blockRegistry.getNameForObject(context.world().getBlock(x, y, z)) + "@" + context.world().getBlockMetadata(x, y, z));
						context.world().removeTileEntity(x, y, z);
						context.world().setBlockToAir(x, y, z);
						return true;
					} else {
						return false;
					}
				}

				// This is slightly hackish, but it's a very important way to verify
				// the stored requirements for anti-cheating purposes.
				if (!context.world().isAirBlock(x, y, z) &&
						getSchematic().getBuildingPermission() == BuildingPermission.ALL &&
						getSchematic() instanceof SchematicBlock) {
					SchematicBlock sb = (SchematicBlock) getSchematic();
					// Copy the old array of stored requirements.
					ItemStack[] oldRequirementsArray = sb.storedRequirements;
					List<ItemStack> oldRequirements = Arrays.asList(oldRequirementsArray);
					sb.storedRequirements = new ItemStack[0];
					sb.storeRequirements(context, x, y, z);
					for (ItemStack s : sb.storedRequirements) {
						boolean contains = false;
						for (ItemStack ss : oldRequirements) {
							if (getSchematic().isItemMatchingRequirement(s, ss)) {
								contains = true;
								break;
							}
						}
						if (!contains) {
							BCLog.logger.warn("Blueprint has MISMATCHING REQUIREMENTS! Potential corrupted/hacked blueprint! Removed mismatched block.");
							BCLog.logger.warn("Location: " + x + ", " + y + ", " + z + " - ItemStack: " + s.toString());
							context.world().removeTileEntity(x, y, z);
							context.world().setBlockToAir(x, y, z);
							return true;
						}
					}
					// Restore the stored requirements.
					sb.storedRequirements = oldRequirementsArray;
				}

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

				return true;
			} catch (Throwable t) {
				t.printStackTrace();
				context.world().setBlockToAir(x, y, z);
				return false;
			}
		}

		return false;
	}

	@Override
	public void postProcessing(IBuilderContext context) {
		getSchematic().postProcessing(context, x, y, z);
	}

	@Override
	public LinkedList<ItemStack> getRequirements(IBuilderContext context) {
		if (mode == Mode.ClearIfInvalid) {
			return new LinkedList<ItemStack>();
		} else {
			LinkedList<ItemStack> req = new LinkedList<ItemStack>();

			getSchematic().getRequirementsForPlacement(context, req);

			return req;
		}
	}

	@Override
	public Position getDestination() {
		return new Position(x + 0.5, y + 0.5, z + 0.5);
	}

	@Override
	public void writeCompleted(IBuilderContext context, double complete) {
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
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
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

		NBTTagList nbtStacks = new NBTTagList();

		if (stackConsumed != null) {
			for (ItemStack stack : stackConsumed) {
				NBTTagCompound nbtStack = new NBTTagCompound();
				stack.writeToNBT(nbtStack);
				nbtStacks.appendTag(nbtStack);
			}
		}

		nbt.setTag("stackConsumed", nbtStacks);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) throws MappingNotFoundException {
		mode = Mode.values()[nbt.getByte("mode")];
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
	public List<ItemStack> getStacksToDisplay() {
		if (mode == Mode.ClearIfInvalid) {
			return stackConsumed;
		} else {
			return getSchematic().getStacksToDisplay(stackConsumed);
		}
	}

	@Override
	public int getEnergyRequirement() {
		return schematic.getEnergyRequirement(stackConsumed);
	}

	@Override
	public int buildTime() {
		if (schematic == null) {
			return 1;
		} else {
			return schematic.buildTime();
		}
	}

}
