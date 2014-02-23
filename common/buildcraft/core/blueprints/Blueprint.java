/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import net.minecraft.block.BlockContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.core.IBptContributor;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.Utils;

public class Blueprint extends BlueprintBase {
	public Blueprint() {
		super ();
	}

	public Blueprint(int sizeX, int sizeY, int sizeZ) {
		super(sizeX, sizeY, sizeZ);
	}

	public void readFromWorld(IBptContext context, TileEntity anchorTile, int x, int y, int z) {
		BptSlot slot = new BptSlot();

		slot.x = (int) (x - context.surroundingBox().pMin().x);
		slot.y = (int) (y - context.surroundingBox().pMin().y);
		slot.z = (int) (z - context.surroundingBox().pMin().z);
		slot.block = anchorTile.getWorldObj().getBlock(x, y, z);
		slot.meta = anchorTile.getWorldObj().getBlockMetadata(x, y, z);

		if (slot.block instanceof BlockContainer) {
			TileEntity tile = anchorTile.getWorldObj().getTileEntity(x, y, z);

			if (tile != null && tile instanceof IBptContributor) {
				IBptContributor contributor = (IBptContributor) tile;

				contributor.saveToBluePrint(anchorTile, this, slot);
			}
		}

		try {
			slot.initializeFromWorld(context, x, y, z);
			contents[slot.x][slot.y][slot.z] = slot;
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
					contents[x][y][z].writeToNBT(cpt, mapping);
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
					contents[x][y][z] = new BptSlot();
					contents[x][y][z].readFromNBT(cpt, mapping);
				}
			}
		}
	}
}
