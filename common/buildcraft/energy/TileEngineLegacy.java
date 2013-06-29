/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.BuildCraftEnergy;
import buildcraft.core.DefaultProps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * This class is just intended to update pre 4.0 engines to the design.
 *
 * It can be deleted someday.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TileEngineLegacy extends TileEngine {

	public TileEngineLegacy() {
		super(0);
	}

	@Override
	public void updateEntity() {
		int meta = getBlockMetadata();
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		TileEntity newTile = BuildCraftEnergy.engineBlock.createTileEntity(worldObj, meta);
		newTile.readFromNBT(nbt);
		worldObj.setBlockTileEntity(xCoord, yCoord, zCoord, newTile);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png";
	}

	@Override
	public float getMaxEnergy() {
		return 1;
	}

	@Override
	public float maxEnergyReceived() {
		return 0;
	}

	@Override
	public float explosionRange() {
		return 0;
	}

	@Override
	public boolean isBurning() {
		return false;
	}

	@Override
	public int getScaledBurnTime(int scale) {
		return 0;
	}

	@Override
	public float getCurrentOutput() {
		return 1;
	}

	@Override
	public float maxEnergyExtracted() {
		return 1;
	}
}
