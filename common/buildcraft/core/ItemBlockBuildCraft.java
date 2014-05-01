/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemBlockBuildCraft extends ItemBlock {

	public ItemBlockBuildCraft(Block b) {
		super(b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addInformation(ItemStack is, EntityPlayer player, List lines, boolean f3) {
		if (is.hasTagCompound() && is.getTagCompound().hasKey("tileData", 10)) {
			NBTTagCompound nbt = is.getTagCompound().getCompoundTag("tileData");
			if (field_150939_a instanceof BlockBuildCraft) {
				((BlockBuildCraft) field_150939_a).addDescription(nbt, lines, f3);
			}
		}
	}

	public boolean placeBlockAt(ItemStack is, EntityPlayer player, World wrd, int x, int y, int z, int s, float hitX, float hitY, float hitZ, int meta) {
		if (!wrd.setBlock(x, y, z, field_150939_a, meta, 3)) {
			return false;
		}
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile != null && is.hasTagCompound() && is.getTagCompound().hasKey("tileData")) {
			NBTTagCompound nbt = is.getTagCompound().getCompoundTag("tileData");
			nbt.setInteger("x", x);
			nbt.setInteger("y", y);
			nbt.setInteger("z", z);
			tile.readFromNBT(nbt);
		}
		if (wrd.getBlock(x, y, z) == field_150939_a) {
			field_150939_a.onBlockPlacedBy(wrd, x, y, z, player, is);
			field_150939_a.onPostBlockPlaced(wrd, x, y, z, meta);
		}
		return true;
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}
}
