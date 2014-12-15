/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.utils.StringUtils;

public class TriggerEnergy extends BCStatement implements ITriggerInternal, ITriggerExternal {

	private boolean high;

	public TriggerEnergy(boolean high) {
		super("buildcraft:energyStored" + (high ? "high" : "low"));

		this.high = high;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.machine.energyStored" + (high ? "High" : "Low"));
	}

	private boolean isTriggeredEnergyHandler(IEnergyConnection connection, ForgeDirection side) {
		int energyStored, energyMaxStored;

		if (connection instanceof IEnergyHandler) {
			energyStored = ((IEnergyHandler) connection).getEnergyStored(side);
			energyMaxStored = ((IEnergyHandler) connection).getMaxEnergyStored(side);
		} else if (connection instanceof IEnergyProvider) {
			energyStored = ((IEnergyProvider) connection).getEnergyStored(side);
			energyMaxStored = ((IEnergyProvider) connection).getMaxEnergyStored(side);
		} else if (connection instanceof IEnergyReceiver) {
			energyStored = ((IEnergyReceiver) connection).getEnergyStored(side);
			energyMaxStored = ((IEnergyReceiver) connection).getMaxEnergyStored(side);
		} else {
			return false;
		}

		if (energyMaxStored > 0) {
			if (high) {
				return (energyStored / energyMaxStored) > 0.95;
			} else {
				return (energyStored / energyMaxStored) < 0.05;
			}
		}
		return false;
	}
	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		if (container instanceof IGate) {
			IGate gate = (IGate) container;
			if (gate.getPipe() instanceof IEnergyHandler) {
				return isTriggeredEnergyHandler((IEnergyHandler) gate.getPipe(), ForgeDirection.UNKNOWN);
			}
		}
		
		return false;
	}


	@Override
	public boolean isTriggerActive(TileEntity tile, ForgeDirection side, IStatementContainer container, IStatementParameter[] parameters) {
		if (tile instanceof IEnergyHandler || tile instanceof IEnergyProvider || tile instanceof IEnergyReceiver) {
			if (((IEnergyConnection) tile).canConnectEnergy(side.getOpposite())) {
				return isTriggeredEnergyHandler((IEnergyConnection) tile, side.getOpposite());
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_machine_energy_" + (high ? "high" : "low"));
	}
}
