/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftEnergy;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.engines.TileEngineWithInventory;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.MathUtils;

public class TileEngineStone extends TileEngineWithInventory {

	static final float MAX_OUTPUT = 10;
	static final float MIN_OUTPUT = MAX_OUTPUT / 3;
	static final float TARGET_OUTPUT = .375f;
	final float kp = 1f;
	final float ki = 0.05f;
	final double eLimit = (MAX_OUTPUT - MIN_OUTPUT) / ki;
	int burnTime = 0;
	int totalBurnTime = 0;
	ItemStack burnItem;
	double esum = 0;

	public TileEngineStone() {
		super(1);
	}

	@Override
	public int getCurrentOutputLimit() {
		return (int) Math.floor((float) getIdealOutput() * heat / IDEAL_HEAT);
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (super.onBlockActivated(player, side)) {
			return true;
		}
		if (!worldObj.isRemote) {
			player.openGui(BuildCraftEnergy.instance, GuiIds.ENGINE_STONE, worldObj, xCoord, yCoord, zCoord);
		}
		return true;
	}

	@Override
	public boolean isBurning() {
		return burnTime > 0;
	}

	@Override
	public void overheat() {
		super.overheat();
		burnTime = 0;
	}

	@Override
	public void burn() {
		if (burnTime > 0) {
			burnTime--;
			if (isRedstonePowered) {
				currentOutput = getIdealOutput();
				addEnergy(currentOutput);
			}
		} else {
			currentOutput = 0;
		}

		if (burnTime == 0 && isRedstonePowered) {
			burnTime = totalBurnTime = getItemBurnTime(getStackInSlot(0));

			if (burnTime > 0) {
				burnItem = getStackInSlot(0);
				setInventorySlotContents(0, InvUtils.consumeItem(getStackInSlot(0)));
			}
		}
	}

	public int getScaledBurnTime(int i) {
		return (int) (((float) burnTime / (float) totalBurnTime) * i);
	}

	private int getItemBurnTime(ItemStack itemstack) {
		if (itemstack == null) {
			return 0;
		} else if (itemstack.getItem() == Items.paper) {
			return 400;
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
	public void sendGUINetworkData(Container containerEngine, ICrafting iCrafting) {
		super.sendGUINetworkData(containerEngine, iCrafting);
		iCrafting.sendProgressBarUpdate(containerEngine, 15, burnTime);
		iCrafting.sendProgressBarUpdate(containerEngine, 16, totalBurnTime);
	}

	@Override
	public int getMaxEnergy() {
		return 10000;
	}

	@Override
	public int getIdealOutput() {
		if (burnItem != null && burnItem.getItem() == Items.paper) {
			return 1;
		}

		double e = TARGET_OUTPUT * getMaxEnergy() - energy;
		esum = MathUtils.clamp(esum + e, -eLimit, eLimit);
		return (int) Math.round(MathUtils.clamp(e * kp + esum * ki, MIN_OUTPUT, MAX_OUTPUT));
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}