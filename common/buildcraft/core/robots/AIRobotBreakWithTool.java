/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;

public class AIRobotBreakWithTool extends AIRobot {

	public BlockIndex blockToBreak;
	private float blockDamage = 0;

	private Block block;
	private int meta;
	private float hardness;
	private float speed;

	public AIRobotBreakWithTool(EntityRobotBase iRobot, BlockIndex iBlockToBreak) {
		super(iRobot, 2);

		blockToBreak = iBlockToBreak;
	}

	@Override
	public void start() {
		robot.aimItemAt(blockToBreak.x, blockToBreak.y, blockToBreak.z);

		robot.setItemActive(true);
		block = robot.worldObj.getBlock(blockToBreak.x, blockToBreak.y, blockToBreak.z);
		meta = robot.worldObj.getBlockMetadata(blockToBreak.x, blockToBreak.y, blockToBreak.z);
		hardness = block.getBlockHardness(robot.worldObj, blockToBreak.x, blockToBreak.y, blockToBreak.z);
		speed = getBreakSpeed(robot, robot.getItemInUse(), block, meta);
	}

	@Override
	public void update() {
		blockDamage += speed / hardness / 30F;

		if (blockDamage > 1.0F) {
			robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), blockToBreak.x,
					blockToBreak.y, blockToBreak.z, -1);
			blockDamage = 0;
			robot.getItemInUse().getItem()
					.onBlockStartBreak(robot.getItemInUse(), blockToBreak.x, blockToBreak.y, blockToBreak.z,
							CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get());
			BlockUtil.breakBlock((WorldServer) robot.worldObj, blockToBreak.x, blockToBreak.y, blockToBreak.z, 6000);
			robot.getItemInUse().getItem().onBlockDestroyed(robot.getItemInUse(), robot.worldObj, block, blockToBreak.x,
					blockToBreak.y, blockToBreak.z, robot);

			if (robot.getItemInUse().getItemDamage() >= robot.getItemInUse().getMaxDamage()) {
				robot.setItemInUse(null);
			}

			terminate();
		} else {
			robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), blockToBreak.x,
					blockToBreak.y, blockToBreak.z, (int) (blockDamage * 10.0F) - 1);
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
		robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), blockToBreak.x,
				blockToBreak.y, blockToBreak.z, -1);
	}

	private float getBreakSpeed(EntityRobotBase robot, ItemStack usingItem, Block block, int meta) {
		ItemStack stack = usingItem;
		float f = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, block, meta);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(robot);
			ItemStack itemstack = usingItem;

			if (i > 0 && itemstack != null) {
				float f1 = i * i + 1;

				boolean canHarvest = ForgeHooks.canToolHarvestBlock(block, meta, itemstack);

				if (!canHarvest && f <= 1.0F) {
					f += f1 * 0.08F;
				} else {
					f += f1;
				}
			}
		}

		return f;
	}
}
