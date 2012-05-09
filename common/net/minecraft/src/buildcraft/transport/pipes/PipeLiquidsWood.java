/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicWood;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsWood extends Pipe implements IPowerReceptor {

	public @TileNetworkData int liquidToExtract;

	private PowerProvider powerProvider;
	private int baseTexture = 7 * 16 + 0;
	private int plainTexture = 1 * 16 + 15;
	private int nextTexture = baseTexture;

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
	public void doWork () {
		if (powerProvider.energyStored <= 0)
			return;

		World w = worldObj;

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (meta > 5)
			return;

		Position pos = new Position(xCoord, yCoord, zCoord,
				Orientations.values()[meta]);
		pos.moveForwards(1);
		int blockId = w.getBlockId((int) pos.x, (int) pos.y,
				(int) pos.z);
		TileEntity tile = w.getBlockTileEntity((int) pos.x, (int) pos.y,
				(int) pos.z);

		if (tile == null
				|| !(tile instanceof ILiquidContainer)
				|| PipeLogicWood
						.isExcludedFromExtraction(Block.blocksList[blockId]))
			return;


	if (tile instanceof ILiquidContainer)
		if (liquidToExtract <= BuildCraftAPI.BUCKET_VOLUME)
			liquidToExtract += powerProvider.useEnergy(1, 1, true)
					* BuildCraftAPI.BUCKET_VOLUME;
	}


	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void updateEntity () {
		super.updateEntity();

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (liquidToExtract > 0 && meta < 6) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[meta]);
			pos.moveForwards(1);

			TileEntity tile = worldObj.getBlockTileEntity((int) pos.x, (int) pos.y,
					(int) pos.z);

			if (tile instanceof ILiquidContainer) {
				ILiquidContainer container = (ILiquidContainer) tile;

				int flowRate = ((PipeTransportLiquids) transport).flowRate;

				int extracted = container.empty(liquidToExtract > flowRate ? flowRate
						: liquidToExtract, false);

				extracted = ((PipeTransportLiquids) transport).fill(
						pos.orientation, extracted, container.getLiquidId(), true);

				container.empty(extracted, true);

				liquidToExtract -= extracted;
			}
		}
	}

	@Override
	public void prepareTextureFor(Orientations connection) {
		if (connection == Orientations.Unknown)
			nextTexture = baseTexture;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == connection.ordinal())
				nextTexture = plainTexture;
			else
				nextTexture = baseTexture;
		}
	}

	@Override
	public int getMainBlockTexture() {
		return nextTexture;
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}
}
