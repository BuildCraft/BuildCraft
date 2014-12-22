/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.RFBattery;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.utils.BlockUtils;

public class TileMiningWell extends TileBuildCraft implements IHasWork, IPipeConnection {
	boolean isDigging = true;
	private BlockMiner miner;

	public TileMiningWell() {
		super();
		this.setBattery(new RFBattery(2 * 64 * BuilderAPI.BREAK_ENERGY, BuilderAPI.BREAK_ENERGY * 4 + BuilderAPI.BUILD_ENERGY, 0));
	}

	/**
	 * Dig the next available piece of land if not done. As soon as it reaches
	 * bedrock, lava or goes below 0, it's considered done.
	 */
	@Override
	public void update() {
		super.update();

		if (worldObj.isRemote) {
			return;
		}

		if (getBattery().getEnergyStored() == 0) {
			return;
		}

		if (miner == null) {
			BlockPos loc = pos.down();
			World world = worldObj;

			while (world.getBlockState(pos).getBlock() == BuildCraftFactory.plainPipeBlock) {
				pos = pos.down();
			}

			if (loc.getY() < 1 || loc.getY() < pos.getY() - BuildCraftFactory.miningDepth || !BlockUtils.canChangeBlock(world, pos)) {
				isDigging = false;
				// Drain energy, because at 0 energy this will stop doing calculations.
				getBattery().useEnergy(0, 10, false);
				return;
			}

			if (world.isAirBlock(pos)) {
				if (getBattery().getEnergyStored() >= BuilderAPI.BUILD_ENERGY) {
					getBattery().useEnergy(BuilderAPI.BUILD_ENERGY, BuilderAPI.BUILD_ENERGY, false);
					world.setBlockState(loc, BuildCraftFactory.plainPipeBlock.getDefaultState());
				}
			} else {
				miner = new BlockMiner(world, this, loc);
			}
		}

		if (miner != null) {
			int usedEnergy = miner.acceptEnergy(getBattery().getEnergyStored());
			getBattery().useEnergy(usedEnergy, usedEnergy, false);

			if (miner.hasMined()) {
				if (miner.hasFailed()) {
					isDigging = false;
				}
				miner = null;
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (miner != null) {
			miner.invalidate();
		}
		if (worldObj != null && pos.getY() > 2) {
			BuildCraftFactory.miningWellBlock.removePipes(worldObj, pos);
		}
	}

	@Override
	public boolean hasWork() {
		return isDigging;
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type,
			EnumFacing with) {
		return type == PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}
}
