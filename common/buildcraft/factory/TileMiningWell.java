/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.core.IMachine;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;

public class TileMiningWell extends TileMachine implements IMachine, IPowerReceptor, IPipeConnection {

	boolean isDigging = true;

	IPowerProvider powerProvider;

	public TileMiningWell() {
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 25, 25, 1000);
	}

	/**
	 * Dig the next available piece of land if not done. As soon as it reaches bedrock, lava or goes below 0, it's considered done.
	 */
	@Override
	public void doWork() {
		if (powerProvider.useEnergy(25, 25, true) < 25)
			return;

		World world = worldObj;

		int depth = yCoord - 1;

		while (world.getBlockId(xCoord, depth, zCoord) == BuildCraftFactory.plainPipeBlock.blockID) {
			depth = depth - 1;
		}

		if (depth < 0 || !BlockUtil.canChangeBlock(world, xCoord, depth, zCoord)) {
			isDigging = false;
			return;
		}

		int blockId = world.getBlockId(xCoord, depth, zCoord);

		List<ItemStack> stacks = BlockUtil.getItemStackFromBlock(worldObj, xCoord, depth, zCoord);

		world.setBlockWithNotify(xCoord, depth, zCoord, BuildCraftFactory.plainPipeBlock.blockID);

		if (blockId == 0)
			return;

		if (stacks == null || stacks.isEmpty())
			return;

		for (ItemStack stack : stacks) {

			ItemStack added = Utils.addToRandomInventory(stack, worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN);
			stack.stackSize -= added.stackSize;
			if (stack.stackSize <= 0) {
				continue;
			}

			if (Utils.addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, stack) && stack.stackSize <= 0)
				return;

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
	public boolean isActive() {
		return isDigging;
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public boolean manageLiquids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) {
		return true;
	}

	@Override
	public boolean allowActions() {
		return false;
	}
}
