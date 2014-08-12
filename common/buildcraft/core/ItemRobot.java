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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.NBTUtils;

public class ItemRobot extends ItemBuildCraft {

	public ItemRobot() {
		super(CreativeTabBuildCraft.BOARDS);
	}

	public EntityRobot createRobot(ItemStack stack, World world) {
		try {
			NBTTagCompound nbt = NBTUtils.getItemData(stack);

			NBTTagCompound boardCpt = nbt.getCompoundTag("board");
			EntityRobot robot = new EntityRobot(world, boardCpt);

			return robot;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public static RedstoneBoardNBT getRobotNBT(ItemStack stack) {
		try {
			NBTTagCompound nbt = NBTUtils.getItemData(stack);

			NBTTagCompound boardCpt = nbt.getCompoundTag("board");
			return RedstoneBoardRegistry.instance.getRedstoneBoard(boardCpt);
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
			RedstoneBoardNBT<?> boardNBT = RedstoneBoardRegistry.instance.getRedstoneBoard(board);

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
			RedstoneBoardNBT<?> nbt = RedstoneBoardRegistry.instance.getRedstoneBoard(cpt);

			if (nbt != null) {
				nbt.addInformation(stack, player, list, advanced);
			}
		}
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		// cancels default BC icon registering
	}

	public static ItemStack createRobotStack(ItemStack board) {
		ItemStack robot = new ItemStack(BuildCraftSilicon.robotItem);
		NBTUtils.getItemData(robot).setTag("board", NBTUtils.getItemData(board));

		return robot;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		itemList.add(new ItemStack(BuildCraftSilicon.robotItem));

		for (RedstoneBoardNBT nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
			ItemStack boardStack = new ItemStack(BuildCraftSilicon.redstoneBoard);
			NBTTagCompound nbtData = NBTUtils.getItemData(boardStack);
			nbt.createBoard(nbtData);

			ItemStack robotStack = createRobotStack(boardStack);

			itemList.add(robotStack.copy());
		}
	}
}
