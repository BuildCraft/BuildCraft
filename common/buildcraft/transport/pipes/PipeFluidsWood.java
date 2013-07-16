/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.network.TileNetworkData;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class PipeFluidsWood extends Pipe implements IPowerReceptor {

	public @TileNetworkData
	int liquidToExtract;
	private PowerHandler powerHandler;
	protected int standardIconIndex = PipeIconProvider.TYPE.PipeFluidsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();
	long lastMining = 0;
	boolean lastPower = false;

	public PipeFluidsWood(int itemID) {
		this(new PipeLogicWood(), itemID);
	}

	protected PipeFluidsWood(PipeLogic logic, int itemID) {
		super(new PipeTransportFluids(), logic, itemID);

		powerHandler = new PowerHandler(this, Type.MACHINE);
		powerHandler.configure(1, 100, 1, 250);
		powerHandler.configurePowerPerdition(0, 0);
	}

	/**
	 * Extracts a random piece of item outside of a nearby chest.
	 */
	@Override
	public void doWork(PowerHandler workProvider) {
		if (powerHandler.getEnergyStored() <= 0)
			return;

		World w = container.worldObj;

		int meta = container.getBlockMetadata();

		if (meta > 5)
			return;

		TileEntity tile = container.getTile(ForgeDirection.getOrientation(meta));

		if (tile instanceof IFluidHandler) {
			if (!PipeManager.canExtractFluids(this, tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord))
				return;

			if (liquidToExtract <= FluidContainerRegistry.BUCKET_VOLUME) {
				liquidToExtract += powerHandler.useEnergy(1, 1, true) * FluidContainerRegistry.BUCKET_VOLUME;
			}
		}
		powerHandler.useEnergy(1, 1, true);
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		int meta = container.getBlockMetadata();

		if (liquidToExtract > 0 && meta < 6) {
			ForgeDirection side = ForgeDirection.getOrientation(meta);
			TileEntity tile = container.getTile(side);

			if (tile instanceof IFluidHandler) {
				IFluidHandler fluidHandler = (IFluidHandler) tile;

				int flowRate = ((PipeTransportFluids) transport).flowRate;

				FluidStack extracted = fluidHandler.drain(side.getOpposite(), liquidToExtract > flowRate ? flowRate : liquidToExtract, false);

				int inserted = 0;
				if (extracted != null) {
					inserted = ((PipeTransportFluids) transport).fill(side, extracted, true);

					fluidHandler.drain(side.getOpposite(), inserted, true);
				}

				liquidToExtract -= inserted;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = container.getBlockMetadata();

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
		}
	}
}
