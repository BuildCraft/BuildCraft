/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * This class is just intended to update pre 4.0 engines to the design.
 *
 * It can be deleted someday.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TileEngineLegacy extends TileEngine {

	private NBTTagCompound nbt;

	@Override
	public void updateEntity() {
		worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord);
		TileEntity newTile = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord);
		if (newTile instanceof TileEngine) {
			newTile.readFromNBT(nbt);
			sendNetworkUpdate();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		nbt = (NBTTagCompound) data.copy();
		this.xCoord = data.getInteger("x");
		this.yCoord = data.getInteger("y");
		this.zCoord = data.getInteger("z");
	}

	@Override
	public ResourceLocation getTextureFile() {
		return WOOD_TEXTURE;
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
