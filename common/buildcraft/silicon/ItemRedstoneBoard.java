/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.NBTUtils;

public class ItemRedstoneBoard extends ItemBuildCraft {

	public IIcon cleanBoard;
	public IIcon usedBoard;

	public ItemRedstoneBoard() {
		super();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("kind") ? 1 : 16;
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		if (!NBTUtils.getItemData(stack).hasKey("kind")) {
			itemIcon = cleanBoard;
		} else {
			itemIcon = usedBoard;
		}

		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		cleanBoard = par1IconRegister.registerIcon("buildcraft:board_clean");
		usedBoard = par1IconRegister.registerIcon("buildcraft:board_used");
	}

}
