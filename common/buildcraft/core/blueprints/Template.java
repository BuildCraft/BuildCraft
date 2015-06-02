/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.builders.blueprints.BlueprintId.Kind;
import buildcraft.core.utils.NBTUtils;

/**
 * Use the template system to describe fillers
 */
public class Template extends BlueprintBase {

	public Template() {
		id.kind = Kind.Template;
	}

	public Template(int sizeX, int sizeY, int sizeZ) {
		super(sizeX, sizeY, sizeZ);

		id.kind = Kind.Template;
	}

	@Override
	public void readFromWorld(IBuilderContext context, TileEntity anchorTile, BlockPos pos) {
		int posX = (int) (pos.getX() - context.surroundingBox().pMin().x);
		int posY = (int) (pos.getY() - context.surroundingBox().pMin().y);
		int posZ = (int) (pos.getZ() - context.surroundingBox().pMin().z);

		if (!BuildCraftAPI.isSoftBlock(anchorTile.getWorld(), pos)) {
			contents[posX][posY][posZ] = new SchematicMask(true);
		}
	}

	@Override
	public void saveContents(NBTTagCompound nbt) {
		// Note: this way of storing data is suboptimal, we really need a bit
		// per mask entry, not a byte. However, this is fine, as compression
		// will fix it.

		byte [] data = new byte[sizeX * sizeY * sizeZ];
		int ind = 0;

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					data [ind] = (byte) ((contents[x][y][z] == null) ? 0 : 1);
					ind++;
				}
			}
		}

		nbt.setByteArray("mask", data);
	}

	@Override
	public void loadContents(NBTTagCompound nbt) throws BptError {
		byte [] data = nbt.getByteArray("mask");
		int ind = 0;

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					if (data [ind] == 1) {
						contents[x][y][z] = new SchematicMask(true);
					}

					ind++;
				}
			}
		}
	}

	@Override
	public ItemStack getStack () {
		ItemStack stack = new ItemStack(BuildCraftBuilders.templateItem, 1, 1);
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		id.write (nbt);
		nbt.setString("author", author);
		nbt.setString("name", id.name);

		return stack;
	}

}
