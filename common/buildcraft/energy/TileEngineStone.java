/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.GuiIds;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.MathUtils;
import buildcraft.energy.gui.ContainerEngine;

public class TileEngineStone extends TileEngineWithInventory {

	static final float MAX_OUTPUT = 1f;
	static final float MIN_OUTPUT = MAX_OUTPUT / 3;
	static final float TARGET_OUTPUT = 0.375f;
	final float kp = 1f;
	final float ki = 0.05f;
	final double eLimit = (MAX_OUTPUT - MIN_OUTPUT) / ki;
	int burnTime = 0;
	int totalBurnTime = 0;
	double esum = 0;

	public TileEngineStone() {
		super(1);
	}

	@Override
	public ResourceLocation getBaseTexture() {
		return BASE_TEXTURES[1];
	}

	@Override
	public ResourceLocation getChamberTexture() {
		return CHAMBER_TEXTURES[1];
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (!worldObj.isRemote) {
			player.openGui(BuildCraftEnergy.instance, GuiIds.ENGINE_STONE, worldObj, xCoord, yCoord, zCoord);
		}
		return true;
	}

	@Override
	public float explosionRange() {
		return 2;
	}

	@Override
	public boolean isBurning() {
		return burnTime > 0;
	}

	@Override
	public void burn() {
		if (burnTime > 0) {
			burnTime--;

			double output = getCurrentOutput();
			currentOutput = output; // Comment out for constant power
			addEnergy(output);
		}

		if (burnTime == 0 && isRedstonePowered) {
			burnTime = totalBurnTime = getItemBurnTime(getStackInSlot(0));

			if (burnTime > 0) {
				setInventorySlotContents(0, InvUtils.consumeItem(getStackInSlot(0)));
			}
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) burnTime / (float) totalBurnTime) * i);
	}

	private int getItemBurnTime(ItemStack itemstack) {
		if (itemstack == null) {
			return 0;
		} else {
			return TileEntityFurnace.getItemBurnTime(itemstack);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		burnTime = data.getInteger("burnTime");
		totalBurnTime = data.getInteger("totalBurnTime");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setInteger("burnTime", burnTime);
		data.setInteger("totalBurnTime", totalBurnTime);
	}

	@Override
	public void getGUINetworkData(int id, int value) {
		super.getGUINetworkData(id, value);
		switch (id) {
			case 15:
				burnTime = value;
				break;
			case 16:
				totalBurnTime = value;
				break;
		}
	}

	@Override
	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		super.sendGUINetworkData(containerEngine, iCrafting);
		iCrafting.sendProgressBarUpdate(containerEngine, 15, burnTime);
		iCrafting.sendProgressBarUpdate(containerEngine, 16, totalBurnTime);
	}

	@Override
	public double maxEnergyReceived() {
		return 200;
	}

	@Override
	public double maxEnergyExtracted() {
		return 100;
	}

	@Override
	public double getMaxEnergy() {
		return 1000;
	}

	@Override
	public double getCurrentOutput() {
		double e = TARGET_OUTPUT * getMaxEnergy() - energy;
		esum = MathUtils.clamp(esum + e, -eLimit, eLimit);
		return MathUtils.clamp(e * kp + esum * ki, MIN_OUTPUT, MAX_OUTPUT);
	}

	@Override
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> triggers = super.getTriggers();
		triggers.add(BuildCraftCore.triggerEmptyInventory);
		triggers.add(BuildCraftCore.triggerContainsInventory);
		triggers.add(BuildCraftCore.triggerSpaceInventory);
		triggers.add(BuildCraftCore.triggerFullInventory);

		return triggers;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}