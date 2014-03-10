/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.Utils;

public class Blueprint extends BlueprintBase {
	public Blueprint() {
		super ();
	}

	public Blueprint(int sizeX, int sizeY, int sizeZ) {
		super(sizeX, sizeY, sizeZ);
	}

	@Override
	public void readFromWorld(IBuilderContext context, TileEntity anchorTile, int x, int y, int z) {
		Block block = anchorTile.getWorldObj().getBlock(x, y, z);

		Schematic slot = SchematicRegistry.newSchematic(block);

		if (slot == null) {
			return;
		}

		int posX = (int) (x - context.surroundingBox().pMin().x);
		int posY = (int) (y - context.surroundingBox().pMin().y);
		int posZ = (int) (z - context.surroundingBox().pMin().z);

		slot.block = block;
		slot.meta = anchorTile.getWorldObj().getBlockMetadata(x, y, z);

		if (slot.block instanceof BlockContainer) {
			TileEntity tile = anchorTile.getWorldObj().getTileEntity(x, y, z);
		}

		try {
			slot.readFromWorld(context, x, y, z);
			contents[posX][posY][posZ] = slot;
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBlueprint", "readFromWorld", t);
		}
	}


	@Override
	public void saveContents(NBTTagCompound nbt) {
		NBTTagList nbtContents = new NBTTagList();

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					NBTTagCompound cpt = new NBTTagCompound();

					if (contents [x][y][z] != null) {
						contents[x][y][z].writeToNBT(cpt, mapping);
					}

					nbtContents.appendTag(cpt);
				}
			}
		}

		nbt.setTag("contents", nbtContents);

		NBTTagCompound contextNBT = new NBTTagCompound();
		mapping.write (contextNBT);
		nbt.setTag("idMapping", contextNBT);
	}

	@Override
	public void loadContents(NBTTagCompound nbt) throws BptError {
		mapping.read (nbt.getCompoundTag("idMapping"));

		NBTTagList nbtContents = nbt.getTagList("contents",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		int index = 0;

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					NBTTagCompound cpt = nbtContents.getCompoundTagAt(index);
					index++;

					if (cpt.hasKey("blockId")) {
						int blockId = cpt.getInteger("blockId");

						contents[x][y][z] = SchematicRegistry.newSchematic(mapping.getBlockForId(blockId));
						contents[x][y][z].readFromNBT(cpt, mapping);
					} else {
						contents[x][y][z] = null;
					}
				}
			}
		}
	}

	@Override
	public ItemStack getStack () {
		ItemStack stack = new ItemStack(BuildCraftBuilders.blueprintItem, 1);
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		id.write (nbt);
		nbt.setString("author", author);

		return stack;
	}
}
