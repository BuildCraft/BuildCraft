/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.List;

import buildcraft.core.proxy.CoreProxy;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.RFBattery;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;

public class TileMiningWell extends TileBuildCraft implements IHasWork, IPipeConnection {

	boolean isDigging = true;

	public TileMiningWell() {
		super();
		this.setBattery(new RFBattery(10000, BuildCraftFactory.MINING_RF_COST_PER_BLOCK, 0));
	}

	/**
	 * Dig the next available piece of land if not done. As soon as it reaches
	 * bedrock, lava or goes below 0, it's considered done.
	 */
	@Override
	public void updateEntity () {
		if (worldObj.isRemote) {
			return;
		}

		int miningCost = (int) Math.ceil(BuildCraftFactory.MINING_RF_COST_PER_BLOCK
				* BuildCraftFactory.miningMultiplier);

		if (getBattery().useEnergy(miningCost, miningCost, false) == 0) {
			return;
		}

		World world = worldObj;

		int depth = yCoord - 1;

		while (world.getBlock(xCoord, depth, zCoord) == BuildCraftFactory.plainPipeBlock) {
			depth = depth - 1;
		}

		if (depth < 1 || depth < yCoord - BuildCraftFactory.miningDepth || !BlockUtil.canChangeBlock(world, xCoord, depth, zCoord)) {
			isDigging = false;
			return;
		}

        BreakEvent breakEvent = new BreakEvent(xCoord, depth, zCoord, worldObj, world.getBlock(xCoord, depth, zCoord),
                world.getBlockMetadata(xCoord, depth, zCoord), CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world).get());
        MinecraftForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            isDigging = false;
            return;
        }

		boolean wasAir = world.isAirBlock(xCoord, depth, zCoord);

		List<ItemStack> stacks = BlockUtil.getItemStackFromBlock((WorldServer) worldObj, xCoord, depth, zCoord);

		world.setBlock(xCoord, depth, zCoord, BuildCraftFactory.plainPipeBlock);

		if (wasAir) {
			return;
		}

		if (stacks == null || stacks.isEmpty()) {
			return;
		}

		for (ItemStack stack : stacks) {

			stack.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, stack);
			if (stack.stackSize <= 0) {
				continue;
			}

			stack.stackSize -= Utils.addToRandomPipeAround(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN, stack);
			if (stack.stackSize <= 0) {
				continue;
			}

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
	public boolean hasWork() {
		return isDigging;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type,
			ForgeDirection with) {
		return type == PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}
}
