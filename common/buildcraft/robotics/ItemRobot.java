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

import cofh.api.energy.IEnergyContainerItem;
import buildcraft.BuildCraftRobotics;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.NBTUtils;

public class ItemRobot extends ItemBuildCraft implements IEnergyContainerItem {

	public ItemRobot() {
		super(CreativeTabBuildCraft.BOARDS);
	}

	public EntityRobot createRobot(ItemStack stack, World world) {
		try {
			NBTTagCompound nbt = NBTUtils.getItemData(stack);

			NBTTagCompound boardCpt = nbt.getCompoundTag("board");
			EntityRobot robot = new EntityRobot(world, boardCpt);
			robot.getBattery().setEnergy(nbt.getInteger("energy"));

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

		int energy = NBTUtils.getItemData(stack).getInteger("energy");
		list.add(energy + "/" + EntityRobotBase.MAX_ENERGY + " RF");
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		// cancels default BC icon registering
	}

	public static ItemStack createRobotStack(ItemStack board, int energy) {
		ItemStack robot = new ItemStack(BuildCraftRobotics.robotItem);
		NBTUtils.getItemData(robot).setTag("board", NBTUtils.getItemData(board));
		NBTUtils.getItemData(robot).setInteger("energy", energy);

		return robot;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		itemList.add(new ItemStack(BuildCraftRobotics.robotItem));

		for (RedstoneBoardNBT nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
			ItemStack boardStack = new ItemStack(BuildCraftRobotics.redstoneBoard);
			NBTTagCompound nbtData = NBTUtils.getItemData(boardStack);
			nbt.createBoard(nbtData);

			ItemStack robotStack = createRobotStack(boardStack, 0);
			itemList.add(robotStack.copy());

			robotStack = createRobotStack(boardStack, EntityRobotBase.MAX_ENERGY);
			itemList.add(robotStack.copy());
		}
	}

	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
		if (!container.hasTagCompound()) {
			return 0;
		}
		int currentEnergy = container.getTagCompound().getInteger("energy");
		int energyReceived = Math.min(EntityRobotBase.MAX_ENERGY - currentEnergy, maxReceive);
		if (!simulate) {
			container.getTagCompound().setInteger("energy", currentEnergy + energyReceived);
		}
		return energyReceived;
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
		if (!container.hasTagCompound()) {
			return 0;
		}
		int currentEnergy = container.getTagCompound().getInteger("energy");
		int energyExtracted = Math.min(currentEnergy, maxExtract);
		if (!simulate) {
			container.getTagCompound().setInteger("energy", currentEnergy - energyExtracted);
		}
		return energyExtracted;
	}

	@Override
	public int getEnergyStored(ItemStack container) {
		if (!container.hasTagCompound()) {
			return 0;
		}
		return container.getTagCompound().getInteger("energy");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {
		if (!container.hasTagCompound()) {
			return 0;
		}
		return EntityRobotBase.MAX_ENERGY;
	}
}
