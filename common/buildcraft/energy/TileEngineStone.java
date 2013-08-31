/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
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
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.energy.gui.ContainerEngine;

public class TileEngineStone extends TileEngineWithInventory {

	final float MAX_OUTPUT = 1f;
	final float MIN_OUTPUT = MAX_OUTPUT / 3;
	final float TARGET_OUTPUT = 0.375f;
	final float kp = 1f;
	final float ki = 0.05f;
	final float eLimit = (MAX_OUTPUT - MIN_OUTPUT) / ki;
	int burnTime = 0;
	int totalBurnTime = 0;
	float esum = 0;

	public TileEngineStone() {
		super(1);
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			player.openGui(BuildCraftEnergy.instance, GuiIds.ENGINE_STONE, worldObj, xCoord, yCoord, zCoord);
		}
		return true;
	}

	@Override
	public ResourceLocation getTextureFile() {
		return STONE_TEXTURE;
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

			float output = getCurrentOutput();
			currentOutput = output; // Comment out for constant power
			addEnergy(output);
		}

		if (burnTime == 0 && isRedstonePowered) {
			burnTime = totalBurnTime = getItemBurnTime(getStackInSlot(0));

			if (burnTime > 0) {
				setInventorySlotContents(0, Utils.consumeItem(getStackInSlot(0)));
			}
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) burnTime / (float) totalBurnTime) * i);
	}

	private int getItemBurnTime(ItemStack itemstack) {
		if (itemstack == null)
			return 0;

		return TileEntityFurnace.getItemBurnTime(itemstack);
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
	public float maxEnergyReceived() {
		return 200;
	}

	@Override
	public float maxEnergyExtracted() {
		return 100;
	}

	@Override
	public float getMaxEnergy() {
		return 1000;
	}

	@Override
	public float getCurrentOutput() {
		float e = TARGET_OUTPUT * getMaxEnergy() - energy;
		esum = Math.min(Math.max(esum + e, -eLimit), eLimit);
		return Math.min(Math.max(e * kp + esum * ki, MIN_OUTPUT), MAX_OUTPUT);
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
}