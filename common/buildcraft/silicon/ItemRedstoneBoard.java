/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.GuiIds;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.NBTUtils;

public class ItemRedstoneBoard extends ItemBuildCraft {

	public IIcon cleanBoard;
	public IIcon unknownBoard;

	public ItemRedstoneBoard() {
		super();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("id") ? 1 : 16;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (cpt.hasKey("id") && !"<unknown>".equals(cpt.getString("id"))) {
			RedstoneBoardRegistry.instance.getRedstoneBoard(cpt).addInformation(stack, player, list, advanced);
		}
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (!cpt.hasKey("id")) {
			itemIcon = cleanBoard;
		} else if ("<unknown>".equals(cpt.getString("id"))) {
			itemIcon = unknownBoard;
		} else {
			itemIcon = RedstoneBoardRegistry.instance.getRedstoneBoard(cpt).getIcon(cpt);
		}

		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		cleanBoard = par1IconRegister.registerIcon("buildcraft:board_clean");
		unknownBoard = par1IconRegister.registerIcon("buildcraft:board_unknown");

		RedstoneBoardRegistry.instance.registerIcons(par1IconRegister);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World world, EntityPlayer entityPlayer) {
		if (!world.isRemote) {
			entityPlayer.openGui(BuildCraftSilicon.instance, GuiIds.REDSTONE_BOARD, world, 0, 0, 0);
		}

		return par1ItemStack;
	}

}
