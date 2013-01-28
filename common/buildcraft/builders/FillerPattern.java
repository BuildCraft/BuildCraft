/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.power.IPowerProvider;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;

public abstract class FillerPattern implements IFillerPattern {

	protected int id;
	protected static float fillEnergy = 25;
	protected static float emptyEnergy = 200;
	protected float lastPowerUsed = 0;

	/**
	 * stackToPlace contains the next item that can be place in the world. Null if there is none. IteratePattern is responsible to decrementing the stack size
	 * if needed. Returns the amount of power used, placing, removing, or doing the action required to iterate the pattern.
	 */
	@Override
	public abstract float iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace, IPowerProvider power);

	@Override
	public abstract String getTextureFile();

	@Override
	public abstract int getTextureIndex();

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public float expectedPowerUse() {
		return lastPowerUsed;
	}

	/**
	 * Attempt to fill blocks in the area.
	 * 
	 * Returns the amount of power used.
	 * 
	 */
	public float fill(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, ItemStack stackToPlace, World world , IPowerProvider power) {
		boolean found = false;
		int xSlot = 0, ySlot = 0, zSlot = 0;
		float energyUsed = 0;

		for (int y = yMin; y <= yMax && !found; ++y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					if (!BlockUtil.canChangeBlock(world, x, y, z))
						return -1;
					if (BlockUtil.isSoftBlock(world, x, y, z)) {
						xSlot = x;
						ySlot = y;
						zSlot = z;

						found = true;
					}
				}
			}
		}

		if (found && stackToPlace != null) {
			energyUsed=power.useEnergy(fillEnergy, fillEnergy, true);
			if(energyUsed>0){
				lastPowerUsed=energyUsed;
				stackToPlace.getItem().onItemUse(stackToPlace, CoreProxy.proxy.getBuildCraftPlayer(world), world, xSlot, ySlot - 1, zSlot, 1, 0.0f, 0.0f, 0.0f);
			}
		}

		return energyUsed;
	}

	/**
	 * Attempt to remove the blocks in the area.
	 * 
	 * Returns the amount of energy used ; 0 if it failed.
	 * 
	 */
	public float empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, World world, IPowerProvider power) {
		boolean found = false;
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;
		float energyUsed = 0;
		
		for (int y = yMin; y <= yMax; ++y) {
			found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (!BlockUtil.canChangeBlock(world, x, y, z))
						return -1;
					if (!BlockUtil.isSoftBlock(world, x, y, z)) {
						found = true;
						lastX = x;
						lastY = y;
						lastZ = z;
					}
				}
			}

			if (found) {
				break;
			}
		}

		if (lastX != Integer.MAX_VALUE) {
			energyUsed=power.useEnergy(emptyEnergy, emptyEnergy, true);
			if(energyUsed>0){
				lastPowerUsed=energyUsed;
				if (BuildCraftBuilders.fillerDestroy) {
					world.setBlockWithNotify(lastX, lastY, lastZ, 0);
				} else {
					BlockUtil.breakBlock(world, lastX, lastY, lastZ);
				}
			}
			return energyUsed;
		}

		return -1;
	}

}
