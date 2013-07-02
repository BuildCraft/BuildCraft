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
import buildcraft.transport.PipeTransportLiquids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeLiquidsWood extends Pipe implements IPowerReceptor {

	public @TileNetworkData
	int liquidToExtract;

	private PowerHandler powerHandler;

	protected int standardIconIndex = PipeIconProvider.TYPE.PipeLiquidsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	long lastMining = 0;
	boolean lastPower = false;

	public PipeLiquidsWood(int itemID) {
		this(new PipeLogicWood(), itemID);
	}
	
	protected PipeLiquidsWood(PipeLogic logic, int itemID) {
		super(new PipeTransportLiquids(), logic, itemID);

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
				liquidToExtract += powerHandler.useEnergy(1, 1, true) * LiquidContainerRegistry.BUCKET_VOLUME;
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
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
		}
	}
}
