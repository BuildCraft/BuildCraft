/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.transport.PipeTransportLiquids;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class PipeLiquidsEmerald extends PipeLiquidsWood {

	public PipeLiquidsEmerald(int itemID) {
		super(new PipeLogicEmerald(), itemID);

		baseTexture = 6 * 16 + 15;
		plainTexture = baseTexture - 1;

		((PipeTransportLiquids) transport).flowRate = 40;
		((PipeTransportLiquids) transport).travelDelay = 4;
	}
	
	/**
	 * Extracts a random piece of item outside of a nearby chest.
	 */
	@Override
	public void doWork() {
		if (powerProvider.getEnergyStored() <= 0)
			return;

		World w = worldObj;

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (meta > 5)
			return;

		Position pos = new Position(xCoord, yCoord, zCoord, ForgeDirection.getOrientation(meta));
		pos.moveForwards(1);
		TileEntity tile = w.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

		if (tile instanceof ITankContainer) {
			if (!PipeManager.canExtractLiquids(this, w, (int) pos.x, (int) pos.y, (int) pos.z))
				return;

			if (liquidToExtract <= LiquidContainerRegistry.BUCKET_VOLUME) {
				liquidToExtract += powerProvider.useEnergy(0.5f, 0.5f, true) * 4 * LiquidContainerRegistry.BUCKET_VOLUME;
			}
		}
	}
}
