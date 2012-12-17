/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.api.core.Position;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.network.TileNetworkData;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsWood extends Pipe implements IPowerReceptor {

	public @TileNetworkData
	int liquidToExtract;

	private IPowerProvider powerProvider;
	private int baseTexture = 7 * 16 + 0;
	private int plainTexture = 1 * 16 + 15;

	long lastMining = 0;
	boolean lastPower = false;

	public PipeLiquidsWood(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicWood(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 1, 1, 1);
		powerProvider.configurePowerPerdition(1, 1);
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
				liquidToExtract += powerProvider.useEnergy(1, 1, true) * LiquidContainerRegistry.BUCKET_VOLUME;
			}
		}
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
	public void updateEntity() {
		super.updateEntity();

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (liquidToExtract > 0 && meta < 6) {
			Position pos = new Position(xCoord, yCoord, zCoord, ForgeDirection.getOrientation(meta));
			pos.moveForwards(1);

			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

			if (tile instanceof ITankContainer) {
				ITankContainer container = (ITankContainer) tile;

				int flowRate = ((PipeTransportLiquids) transport).flowRate;

				LiquidStack extracted = container.drain(pos.orientation.getOpposite(), liquidToExtract > flowRate ? flowRate : liquidToExtract, false);

				int inserted = 0;
				if (extracted != null) {
					inserted = ((PipeTransportLiquids) transport).fill(pos.orientation, extracted, true);

					container.drain(pos.orientation.getOpposite(), inserted, true);
				}

				liquidToExtract -= inserted;
			}
		}
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return baseTexture;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return plainTexture;
			else
				return baseTexture;
		}
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().getMaxEnergyReceived();
	}

	@Override
	public boolean canConnectRedstone() {
		if (PowerFramework.currentFramework instanceof RedstonePowerFramework)
			return true;
		return super.canConnectRedstone();
	}
}
