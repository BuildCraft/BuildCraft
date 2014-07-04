/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.IGate;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.utils.StringUtils;

public class TriggerEnergy extends BCTrigger {

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

	@Override
	public boolean isTriggerActive(IGate gate, ITriggerParameter[] parameters) {
		IBatteryObject battery = MjAPI.getMjBattery(gate.getPipe());

		if (battery != null) {
			double maxCapacity = battery.maxCapacity();

			if (maxCapacity > 0) {
				if (high) {
					return battery.getEnergyStored() / maxCapacity > 0.95;
				} else {
					return battery.getEnergyStored() / maxCapacity < 0.05;
				}
			}
		}

		// if the pipe can't set the trigger one way or the other, then look
		// around.
		return super.isTriggerActive(gate, parameters);
	}


	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		IBatteryObject battery = MjAPI.getMjBattery(tile);

		if (battery != null) {
			double maxCapacity = battery.maxCapacity();

			if (maxCapacity > 0) {
				if (high) {
					return battery.getEnergyStored() / maxCapacity > 0.95;
				} else {
					return battery.getEnergyStored() / maxCapacity < 0.05;
				}
			}
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
