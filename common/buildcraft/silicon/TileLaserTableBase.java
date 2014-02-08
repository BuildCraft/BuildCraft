/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.api.power.ILaserTarget;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class TileLaserTableBase extends TileBuildCraft implements ILaserTarget, IInventory {

	public double clientRequiredEnergy = 0;
	protected SimpleInventory inv = new SimpleInventory(getSizeInventory(), "inv", 64);
	private double[] recentEnergy = new double[20];
	private double energy = 0;
	private int tick = 0;
	private int recentEnergyAverage;

	@Override
	public void updateEntity() {
		tick++;
		tick = tick % recentEnergy.length;
		recentEnergy[tick] = 0.0f;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public void addEnergy(double energy) {
		this.energy += energy;
	}

	public void subtractEnergy(double energy) {
		this.energy -= energy;
	}

	public abstract double getRequiredEnergy();

	public int getProgressScaled(int ratio) {
		if (clientRequiredEnergy == 0.0)
			return 0;
		if (energy >= clientRequiredEnergy)
			return ratio;
		return (int) (energy / clientRequiredEnergy * ratio);
	}

	public int getRecentEnergyAverage() {
		return recentEnergyAverage;
	}

	public abstract boolean canCraft();

	@Override
	public boolean requiresLaserEnergy() {
		return canCraft() && energy < getRequiredEnergy() * 5F;
	}

	@Override
	public void receiveLaserEnergy(double energy) {
		this.energy += energy;
		recentEnergy[tick] += energy;
	}

	@Override
	public boolean isInvalidTarget() {
		return isInvalid();
	}

	@Override
	public int getXCoord() {
		return xCoord;
	}

	@Override
	public int getYCoord() {
		return yCoord;
	}

	@Override
	public int getZCoord() {
		return zCoord;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return inv.decrStackSize(slot, amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inv.setInventorySlotContents(slot, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && !isInvalid();
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		inv.writeToNBT(nbt, "inv");
		nbt.setDouble("energy", energy);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		inv.readFromNBT(nbt, "inv");
		energy = nbt.getDouble("energy");
	}

	public void getGUINetworkData(int id, int data) {
		int currentStored = (int) (energy * 100.0);
		int requiredEnergy = (int) (clientRequiredEnergy * 100.0);
		switch (id) {
			case 0:
				requiredEnergy = (requiredEnergy & 0xFFFF0000) | (data & 0xFFFF);
				clientRequiredEnergy = (requiredEnergy / 100.0f);
				break;
			case 1:
				currentStored = (currentStored & 0xFFFF0000) | (data & 0xFFFF);
				energy = (currentStored / 100.0f);
				break;
			case 2:
				requiredEnergy = (requiredEnergy & 0xFFFF) | ((data & 0xFFFF) << 16);
				clientRequiredEnergy = (requiredEnergy / 100.0f);
				break;
			case 3:
				currentStored = (currentStored & 0xFFFF) | ((data & 0xFFFF) << 16);
				energy = (currentStored / 100.0f);
				break;
			case 4:
				recentEnergyAverage = recentEnergyAverage & 0xFFFF0000 | (data & 0xFFFF);
				break;
			case 5:
				recentEnergyAverage = (recentEnergyAverage & 0xFFFF) | ((data & 0xFFFF) << 16);
				break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		int requiredEnergy = (int) (getRequiredEnergy() * 100.0);
		int currentStored = (int) (energy * 100.0);
		int lRecentEnergy = 0;
		for (int i = 0; i < recentEnergy.length; i++) {
			lRecentEnergy += (int) (recentEnergy[i] * 100.0 / (recentEnergy.length - 1));
		}
		iCrafting.sendProgressBarUpdate(container, 0, requiredEnergy & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 1, currentStored & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 2, (requiredEnergy >>> 16) & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 3, (currentStored >>> 16) & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 4, lRecentEnergy & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 5, (lRecentEnergy >>> 16) & 0xFFFF);
	}
}
