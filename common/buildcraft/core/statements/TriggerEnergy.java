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
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.utils.StringUtils;

public class TriggerEnergy extends BCStatement implements ITriggerInternal, ITriggerExternal {

	private boolean high;
	private IIcon iconEnergyLow;
	private IIcon iconEnergyHigh;

	public TriggerEnergy(boolean high) {
		super("buildcraft:energyStored" + (high ? "high" : "low"));

		this.high = high;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.machine.energyStored" + (high ? "High" : "Low"));
	}

	private boolean isValidEnergyHandler(IEnergyHandler handler) {
		return handler.getMaxEnergyStored(ForgeDirection.UNKNOWN) > 0;
	}

	private boolean isTriggeredEnergyHandler(IEnergyHandler handler) {
		int energyStored = handler.getEnergyStored(ForgeDirection.UNKNOWN);
		int energyMaxStored = handler.getMaxEnergyStored(ForgeDirection.UNKNOWN);

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
					return isTriggeredEnergyHandler((IEnergyHandler) gate.getPipe());
				}
			}
		}
		
		return false;
	}


	@Override
	public boolean isTriggerActive(TileEntity tile, ForgeDirection side, IStatementContainer container, IStatementParameter[] parameters) {
		if (tile instanceof IEnergyHandler) {
			// Since we return false upon the trigger being invalid anyway,
			// we can skip the isValidEnergyHandler(...) check.
			return isTriggeredEnergyHandler((IEnergyHandler) tile);
		}

		return false;
	}

	@Override
	public IIcon getIcon() {
		if (high) {
			return iconEnergyHigh;
		} else {
			return iconEnergyLow;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		iconEnergyHigh = iconRegister.registerIcon("buildcraft:triggers/trigger_machine_energy_high");
		iconEnergyLow = iconRegister.registerIcon("buildcraft:triggers/trigger_machine_energy_low");
	}
}
