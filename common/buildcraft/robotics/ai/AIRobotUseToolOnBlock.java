/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.proxy.CoreProxy;

public class AIRobotUseToolOnBlock extends AIRobot {

	private BlockIndex useToBlock;
	private int useCycles = 0;

	public AIRobotUseToolOnBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotUseToolOnBlock(EntityRobotBase iRobot, BlockIndex index) {
		this(iRobot);

		useToBlock = index;
	}

	@Override
	public void start() {
		robot.aimItemAt(useToBlock.x, useToBlock.y, useToBlock.z);
		robot.setItemActive(true);
	}

	@Override
	public void update() {
		useCycles++;

		if (useCycles > 40) {
			ItemStack stack = robot.getHeldItem();

			EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj)
					.get();
			if (BlockUtils.useItemOnBlock(robot.worldObj, player, stack, useToBlock.x,
					useToBlock.y, useToBlock.z, ForgeDirection.UP)) {
				if (robot.getHeldItem().isItemStackDamageable()) {
					robot.getHeldItem().damageItem(1, robot);

					if (robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage()) {
						robot.setItemInUse(null);
					}
				} else {
					robot.setItemInUse(null);
				}
			} else {
				setSuccess(false);
				if (!robot.getHeldItem().isItemStackDamageable()) {
					BlockUtils.dropItem((WorldServer) robot.worldObj,
							MathHelper.floor_double(robot.posX),
							MathHelper.floor_double(robot.posY),
							MathHelper.floor_double(robot.posZ), 6000, stack);
					robot.setItemInUse(null);
				}
			}

			terminate();
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
	}

	@Override
	public int getEnergyCost() {
		return 8;
	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (useToBlock != null) {
			NBTTagCompound sub = new NBTTagCompound();
			useToBlock.writeTo(sub);
			nbt.setTag("blockFound", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockFound")) {
			useToBlock = new BlockIndex(nbt.getCompoundTag("blockFound"));
		}
	}
}
