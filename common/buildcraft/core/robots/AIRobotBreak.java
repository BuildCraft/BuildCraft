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
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtils;
import buildcraft.core.utils.Utils;

public class AIRobotBreak extends AIRobot {

	public BlockPos blockToBreak;
	private float blockDamage = 0;

	private IBlockState state;
	private float hardness;
	private float speed;

	public AIRobotBreak(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotBreak(EntityRobotBase iRobot, BlockPos iBlockToBreak) {
		super(iRobot);

		blockToBreak = iBlockToBreak;
	}

	@Override
	public void start() {
		robot.aimItemAt(blockToBreak);

		robot.setItemActive(true);
		state = robot.worldObj.getBlockState(blockToBreak);
		hardness = state.getBlock().getBlockHardness(robot.worldObj, blockToBreak);
		speed = getBreakSpeed(robot, robot.getHeldItem());
	}

	@Override
	public void update() {
		if (hardness == 0) {
			// defensive code
			terminate();
			return;
		}

		blockDamage += speed / hardness / 30F;

		if (blockDamage > 1.0F) {
			robot.worldObj.sendBlockBreakProgress(robot.getEntityId(), blockToBreak, -1);
			blockDamage = 0;

			if (robot.getHeldItem() != null) {
				robot.getHeldItem().getItem()
						.onBlockStartBreak(robot.getHeldItem(), blockToBreak,
							CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get());
			}

			if (BlockUtils.breakBlock((WorldServer) robot.worldObj, blockToBreak, 6000)) {
				robot.worldObj.playAuxSFXAtEntity(null, 2001,
						blockToBreak, Block.getStateId(state));

				if (robot.getHeldItem() != null) {
					robot.getHeldItem().getItem()
							.onBlockDestroyed(robot.getHeldItem(), robot.worldObj, state.getBlock(),
									blockToBreak, robot);

					if (robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage()) {
						robot.setItemInUse(null);
					}
				}
			}

			terminate();
		} else {
			robot.worldObj.sendBlockBreakProgress(robot.getEntityId(), blockToBreak, (int) (blockDamage * 10.0F) - 1);
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
		robot.worldObj.sendBlockBreakProgress(robot.getEntityId(), blockToBreak, -1);
	}

	private float getBreakSpeed(EntityRobotBase robot, ItemStack usingItem) {
		ItemStack stack = usingItem;
		float f = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, state);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(robot);
			ItemStack itemstack = usingItem;

			if (i > 0 && itemstack != null) {
				float f1 = i * i + 1;

				boolean canHarvest = ForgeHooks.canToolHarvestBlock(robot.worldObj, blockToBreak, itemstack);

				if (!canHarvest && f <= 1.0F) {
					f += f1 * 0.08F;
				} else {
					f += f1;
				}
			}
		}

		return f;
	}

	@Override
	public int getEnergyCost() {
		return 20;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockToBreak != null) {
			NBTTagCompound sub = new NBTTagCompound();
			Utils.writeBlockPos(sub, blockToBreak);
			nbt.setTag("blockToBreak", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockToBreak")) {
			blockToBreak = Utils.readBlockPos(nbt.getCompoundTag("blockToBreak"));
		}
	}
}
