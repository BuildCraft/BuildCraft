/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.BuildCraftRobotics;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.NBTUtils;

public class ItemRedstoneBoard extends ItemBuildCraft {
	public ItemRedstoneBoard() {
		super(BCCreativeTab.get("boards"));
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return NBTUtils.getItemData(stack).hasKey("id") ? 1 : 16;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (cpt.hasKey("id") && !"<unknown>".equals(cpt.getString("id"))) {
			RedstoneBoardNBT board = RedstoneBoardRegistry.instance.getRedstoneBoard(cpt);
			if (board != null) {
				board.addInformation(stack, player, list, advanced);
			} else {
				list.add(EnumChatFormatting.BOLD + "Corrupt board!");
			}
		}
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack);

		if (!cpt.hasKey("id")) {
			itemIcon = icons[0];
		} else if ("<unknown>".equals(cpt.getString("id"))) {
			itemIcon = icons[1];
		} else {
			RedstoneBoardNBT board = RedstoneBoardRegistry.instance.getRedstoneBoard(cpt);
			if (board != null) {
				itemIcon = board.getIcon(cpt);
			} else {
				itemIcon = icons[1];
			}
		}

		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String[] getIconNames() {
		return new String[]{ "board/clean", "board/unknown" };
	}


	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
		itemList.add(stack);
		for (RedstoneBoardNBT nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
			stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
			NBTTagCompound nbtData = NBTUtils.getItemData(stack);
			nbt.createBoard(nbtData);
			itemList.add(stack.copy());
		}
	}

	public static boolean isClean(ItemStack stack) {
		return !stack.hasTagCompound() || !stack.getTagCompound().hasKey("id");
	}
}
