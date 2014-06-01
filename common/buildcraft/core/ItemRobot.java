/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import buildcraft.api.boards.IRedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.NBTUtils;

public class ItemRobot extends ItemBuildCraft {

	public ItemRobot() {
		super(CreativeTabBuildCraft.ITEMS);
	}

	public EntityRobot createRobot(ItemStack stack, World world) {
		try {
			IRedstoneBoardRobot board = null;
			NBTTagCompound nbt = NBTUtils.getItemData(stack);

			if (nbt.hasKey("board")) {
				NBTTagCompound boardCpt = nbt.getCompoundTag("board");
				RedstoneBoardNBT boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(boardCpt);

				if (boardNBT instanceof RedstoneBoardRobotNBT) {
					board = ((RedstoneBoardRobotNBT) boardNBT).create(boardCpt);
				}
			}

			EntityRobot robot = new EntityRobot(world, board);

			return robot;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResourceLocation getTextureRobot(ItemStack stack) {
		NBTTagCompound nbt = NBTUtils.getItemData(stack);

		if (!nbt.hasKey("board")) {
			return EntityRobot.ROBOT_BASE;
		} else {
			NBTTagCompound board = nbt.getCompoundTag("board");
			RedstoneBoardNBT boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(board);

			if (boardNBT instanceof RedstoneBoardRobotNBT) {
				return ((RedstoneBoardRobotNBT) boardNBT).getRobotTexture();
			} else {
				return EntityRobot.ROBOT_BASE;
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		NBTTagCompound cpt = NBTUtils.getItemData(stack).getCompoundTag("board");

		if (cpt.hasKey("id") && !"<unknown>".equals(cpt.getString("id"))) {
			RedstoneBoardRegistry.instance.getRedstoneBoard(cpt).addInformation(stack, player, list, advanced);
		}

	}

}
