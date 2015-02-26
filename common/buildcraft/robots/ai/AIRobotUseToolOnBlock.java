/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.ai;

import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.proxy.CoreProxy;

public class AIRobotUseToolOnBlock extends AIRobot {

	private BlockIndex useToBlock;
	private int useCycles = 0;

	public AIRobotUseToolOnBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotUseToolOnBlock(EntityRobotBase iRobot, BlockIndex index) {
		super(iRobot);

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

			if (robot.getHeldItem().isItemStackDamageable()) {
				robot.getHeldItem().damageItem(1, robot);

				if (robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage()) {
					robot.setItemInUse(null);
				}
			} else {
				robot.setItemInUse(null);
			}

			stack.getItem().onItemUse(stack, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get(),
					robot.worldObj, useToBlock.x, useToBlock.y, useToBlock.z, ForgeDirection.UP.ordinal(), 0, 0, 0);

			terminate();
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
	}

	@Override
	public int getEnergyCost() {
		return 30;
	}
}
