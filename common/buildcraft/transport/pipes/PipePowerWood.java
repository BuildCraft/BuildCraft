/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IconConstants;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;

public class PipePowerWood extends Pipe implements IPowerReceptor {

	private static final int MAX_OVERHEAT_TICKS = 100;

	private IPowerProvider powerProvider;
	
	protected int standardIconIndex = IconConstants.PipePowerWood_Standard;
	protected int solidIconIndex = IconConstants.PipeAllWood_Solid;


	private int overheatTicks;

	public PipePowerWood(int itemID) {
		super(new PipeTransportPower(), new PipeLogicWood(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 2, 1000, 1, 1000);
		powerProvider.configurePowerPerdition(1, 100);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon[] getTextureIcons() {
		return BuildCraftTransport.instance.icons;
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

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		if (overheatTicks > 0)
			return null;
		return powerProvider;
	}

	@Override
	public void doWork() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (worldObj.isRemote)
			return;

		if (powerProvider.getEnergyStored() == powerProvider.getMaxEnergyStored()) {
			overheatTicks += overheatTicks < MAX_OVERHEAT_TICKS ? 1 : 0;
		} else {
			overheatTicks -= overheatTicks > 0 ? 1 : 0;
		}

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (Utils.checkPipesConnections(container, container.getTile(o))) {
				TileEntity tile = container.getTile(o);

				if (tile instanceof TileGenericPipe) {
					if (((TileGenericPipe) tile).pipe == null) {
						continue; // Null pointer protection
					}

					PipeTransportPower trans = (PipeTransportPower) ((TileGenericPipe) tile).pipe.transport;

					float energyToRemove;

					if (powerProvider.getEnergyStored() > 40) {
						energyToRemove = powerProvider.getEnergyStored() / 40 + 4;
					} else if (powerProvider.getEnergyStored() > 10) {
						energyToRemove = powerProvider.getEnergyStored() / 10;
					} else {
						energyToRemove = 1;
					}

					float energyUsed = powerProvider.useEnergy(1, energyToRemove, true);

					trans.receiveEnergy(o.getOpposite(), energyUsed);
				}
			}
		}
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().getMaxEnergyReceived();
	}

}
