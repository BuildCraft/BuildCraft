/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import java.lang.reflect.Field;

import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.MjBattery;

/**
 * Creates a deep copy of an existing IInventory.
 *
 * Useful for performing inventory manipulations and then examining the results
 * without affecting the original inventory.
 */
public class BatteryCopy implements IBatteryObject {

	private IBatteryObject orignal;
	private double contents;

	public BatteryCopy(IBatteryObject orignal) {
		this.orignal = orignal;
		contents = orignal.getEnergyStored();
	}

	@Override
	public double getEnergyRequested() {
		return 0;
	}

	@Override
	public double addEnergy(double mj) {
		return 0;
	}

	@Override
	public double addEnergy(double mj, boolean ignoreCycleLimit) {
		return 0;
	}

	@Override
	public double getEnergyStored() {
		return contents;
	}

	@Override
	public void setEnergyStored(double mj) {
		contents = mj;
	}

	@Override
	public double maxCapacity() {
		return orignal.maxCapacity();
	}

	@Override
	public double minimumConsumption() {
		return orignal.minimumConsumption();
	}

	@Override
	public double maxReceivedPerCycle() {
		return orignal.maxReceivedPerCycle();
	}

	@Override
	public String kind() {
		return orignal.kind();
	}

	@Override
	public void init(Object object, Field storedField, MjBattery battery) {

	}


}
