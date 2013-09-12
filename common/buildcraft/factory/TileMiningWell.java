/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileMiningWell extends TileBuildCraft implements IMachine, IPowerReceptor {

	boolean isDigging = true;
	private PowerHandler powerHandler;

	public TileMiningWell() {
		powerHandler = new PowerHandler(this, Type.MACHINE);

		float mj = BuildCraftFactory.MINING_MJ_COST_PER_BLOCK * BuildCraftFactory.miningMultiplier;
		powerHandler.configure(100 * BuildCraftFactory.miningMultiplier, 100 * BuildCraftFactory.miningMultiplier, mj, 1000 * BuildCraftFactory.miningMultiplier);
		powerHandler.configurePowerPerdition(1, 1);
	}

	/**
	 * Dig the next available piece of land if not done. As soon as it reaches
	 * bedrock, lava or goes below 0, it's considered done.
	 */
	@Override
	public void doWork(PowerHandler workProvider) {
		float mj = BuildCraftFactory.MINING_MJ_COST_PER_BLOCK * BuildCraftFactory.miningMultiplier;
		if (powerHandler.useEnergy(mj, mj, true) != mj)
			return;

		World world = worldObj;

		int depth = yCoord - 1;

		while (world.getBlockId(xCoord, depth, zCoord) == BuildCraftFactory.plainPipeBlock.blockID) {
			depth = depth - 1;
		}

		if (depth < 1 || depth < yCoord - BuildCraftFactory.miningDepth || !BlockUtil.canChangeBlock(world, xCoord, depth, zCoord)) {
			isDigging = false;
			return;
		}

		boolean wasAir = world.isAirBlock(xCoord, depth, zCoord);

		List<ItemStack> stacks = BlockUtil.getItemStackFromBlock(worldObj, xCoord, depth, zCoord);

		world.setBlock(xCoord, depth, zCoord, BuildCraftFactory.plainPipeBlock.blockID);

		if (wasAir)
			return;

		if (stacks == null || stacks.isEmpty())
			return;

		for (ItemStack stack : stacks) {

			stack.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, stack);
			if (stack.stackSize <= 0)
				continue;

			stack.stackSize -= Utils.addToRandomPipeAround(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN, stack);
			if (stack.stackSize <= 0)
				continue;

			// Throw the object away.
			// TODO: factorize that code

			float f = world.rand.nextFloat() * 0.8F + 0.1F;
			float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
			float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

			EntityItem entityitem = new EntityItem(world, xCoord + f, yCoord + f1 + 0.5F, zCoord + f2, stack);

			entityitem.lifespan = BuildCraftCore.itemLifespan;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.05F;
			entityitem.motionX = (float) world.rand.nextGaussian() * f3;
			entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 1.0F;
			entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
			world.spawnEntityInWorld(entityitem);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (worldObj != null && yCoord > 2) {
			BuildCraftFactory.miningWellBlock.removePipes(worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public boolean isActive() {
		return isDigging;
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public boolean allowAction(IAction action) {
		return false;
	}
}
