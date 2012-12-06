/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

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
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

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

		ForgeDirection direction = ((PipeLogicWood) logic).direction;
		if (direction == ForgeDirection.UNKNOWN)
			return;

		TileEntity tile = container.getTile(direction);

		if (tile instanceof ITankContainer) {
         if (!PipeManager.canExtractLiquids(this, worldObj, tile.xCoord, tile.yCoord, tile.zCoord))
            return;

         if (liquidToExtract <= LiquidContainerRegistry.BUCKET_VOLUME)
            liquidToExtract += powerProvider.useEnergy(1, 1, true) * LiquidContainerRegistry.BUCKET_VOLUME;
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

		ForgeDirection direction = ((PipeLogicWood) logic).direction;
		if (liquidToExtract > 0 && direction != ForgeDirection.UNKNOWN) {
			TileEntity tile = container.getTile(direction);

			if (tile instanceof ITankContainer) {
				ITankContainer container = (ITankContainer) tile;
				int flowRate = ((PipeTransportLiquids) transport).flowRate;
				LiquidStack extracted = container.drain(direction.getOpposite(), liquidToExtract > flowRate ? flowRate : liquidToExtract, false);
                int inserted = 0;

                if(extracted != null) {
                    inserted = ((PipeTransportLiquids) transport).fill(direction, extracted, true);

                    container.drain(direction.getOpposite(), inserted, true);
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
			if (((PipeLogicWood) logic).direction == direction)
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
		if(PowerFramework.currentFramework instanceof RedstonePowerFramework)
			return true;
		return super.canConnectRedstone();
	}
}
