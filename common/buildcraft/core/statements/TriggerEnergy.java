/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.utils.StringUtils;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

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

	@Override
	public int getSheetLocation() {
		return 12 + (high ? 1 : 2) * 16;
	}

	private boolean isValidEnergyHandler(IEnergyHandler handler) {
		return handler != null;
	}

	private boolean isTriggeredEnergyHandler(IEnergyConnection connection, EnumFacing side) {
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
				if (isValidEnergyHandler((IEnergyHandler) gate.getPipe())) {
					return isTriggeredEnergyHandler((IEnergyHandler) gate.getPipe(), null);
				}
			}
		}
		
		return false;
	}


	@Override
	public boolean isTriggerActive(TileEntity tile, EnumFacing side, IStatementContainer container, IStatementParameter[] parameters) {
		if (tile instanceof IEnergyHandler || tile instanceof IEnergyProvider || tile instanceof IEnergyReceiver) {
			// Since we return false upon the trigger being invalid anyway,
			// we can skip the isValidEnergyHandler(...) check.
			if (((IEnergyConnection) tile).canConnectEnergy(side.getOpposite())) {
				return isTriggeredEnergyHandler((IEnergyConnection) tile, side.getOpposite());
			}
		}

		return false;
	}
}
