/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.lib.utils.BlockUtils;

public class AIRobotBreak extends AIRobot {

	public BlockIndex blockToBreak;
	private float blockDamage = 0;

	private Block block;
	private int meta;
	private float hardness;
	private float speed;

	public AIRobotBreak(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotBreak(EntityRobotBase iRobot, BlockIndex iBlockToBreak) {
		super(iRobot);

		blockToBreak = iBlockToBreak;
	}

	@Override
	public void start() {
		robot.aimItemAt(blockToBreak.x, blockToBreak.y, blockToBreak.z);

		robot.setItemActive(true);
		block = robot.worldObj.getBlock(blockToBreak.x, blockToBreak.y, blockToBreak.z);
		meta = robot.worldObj.getBlockMetadata(blockToBreak.x, blockToBreak.y, blockToBreak.z);
		hardness = block.getBlockHardness(robot.worldObj, blockToBreak.x, blockToBreak.y, blockToBreak.z);
		speed = getBreakSpeed(robot, robot.getHeldItem(), block, meta);
	}

	@Override
	public void update() {

		if (block == null || block.isAir(robot.worldObj, blockToBreak.x, blockToBreak.y, blockToBreak.z)) {
			terminate();
		}

		if (hardness != 0) {
			blockDamage += speed / hardness / 30F;
		} else {
			// Instantly break the block
			blockDamage = 1.1F;
		}

		if (blockDamage > 1.0F) {
			robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), blockToBreak.x,
				blockToBreak.y, blockToBreak.z, -1);
			blockDamage = 0;

			if (robot.getHeldItem() != null) {
				robot.getHeldItem().getItem()
					.onBlockStartBreak(robot.getHeldItem(), blockToBreak.x, blockToBreak.y, blockToBreak.z,
						CoreProxy.proxy.getBuildCraftPlayer((WorldServer) robot.worldObj).get());
			}

			if (BlockUtils.breakBlock((WorldServer) robot.worldObj, blockToBreak.x, blockToBreak.y, blockToBreak.z, 6000)) {
				robot.worldObj.playAuxSFXAtEntity(null, 2001,
					blockToBreak.x, blockToBreak.y, blockToBreak.z,
					Block.getIdFromBlock(block) + (meta << 12));

				if (robot.getHeldItem() != null) {
					robot.getHeldItem().getItem()
						.onBlockDestroyed(robot.getHeldItem(), robot.worldObj, block, blockToBreak.x,
							blockToBreak.y, blockToBreak.z, robot);

					if (robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage()) {
						robot.setItemInUse(null);
					}
				}
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

	@Override
	public int getEnergyCost() {
		return (int) Math.ceil((float) BuilderAPI.BREAK_ENERGY * 2 / 30.0F);
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockToBreak != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockToBreak.writeTo(sub);
			nbt.setTag("blockToBreak", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockToBreak")) {
			blockToBreak = new BlockIndex(nbt.getCompoundTag("blockToBreak"));
		}
	}
}
