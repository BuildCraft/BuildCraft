/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import net.minecraft.block.Block;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineCoolant.Coolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.liquids.LiquidUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import static buildcraft.energy.TileEngine.EnergyStage.BLUE;
import static buildcraft.energy.TileEngine.EnergyStage.GREEN;
import static buildcraft.energy.TileEngine.EnergyStage.RED;
import static buildcraft.energy.TileEngine.EnergyStage.YELLOW;
import static buildcraft.energy.TileEngine.IDEAL_HEAT;
import static buildcraft.energy.TileEngine.MIN_HEAT;
import buildcraft.energy.gui.ContainerEngine;
import java.util.LinkedList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.liquids.ITankContainer;

public class TileEngineIron extends TileEngine implements ITankContainer {

	public static int MAX_LIQUID = LiquidContainerRegistry.BUCKET_VOLUME * 10;
	public static float HEAT_PER_MJ = 0.0023F;
	public static float COOLDOWN_RATE = 0.005F;
	int burnTime = 0;
	private LiquidTank fuelTank;
	private LiquidTank coolantTank;
	private IronEngineFuel currentFuel = null;
	public int penaltyCooling = 0;
	boolean lastPowered = false;

	public TileEngineIron() {
		super(1);
		fuelTank = new LiquidTank(MAX_LIQUID);
		coolantTank = new LiquidTank(MAX_LIQUID);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png";
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (player.getCurrentEquippedItem() != null) {
			if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
			ItemStack current = player.getCurrentEquippedItem();
			if (current != null && current.itemID != Item.bucketEmpty.itemID) {
				if (CoreProxy.proxy.isSimulating(worldObj)) {
					if (LiquidUtils.handleRightClick(this, side, player, true, false)) {
						return true;
					}
				} else {
					if (LiquidContainerRegistry.isContainer(current)) {
						return true;
					}
				}
			}
		}
		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			player.openGui(BuildCraftEnergy.instance, GuiIds.ENGINE_IRON, worldObj, xCoord, yCoord, zCoord);
		}
		return true;
	}

	@Override
	public float explosionRange() {
		return 4;
	}

	@Override
	public float getPistonSpeed() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			return Math.max(0.07f * getHeatLevel(), 0.01f);
		}
		switch (getEnergyStage()) {
			case BLUE:
				return 0.04F;
			case GREEN:
				return 0.05F;
			case YELLOW:
				return 0.06F;
			case RED:
				return 0.07F;
			default:
				return 0;
		}
	}

	@Override
	public boolean isBurning() {
		LiquidStack fuel = fuelTank.getLiquid();
		return fuel != null && fuel.amount > 0 && penaltyCooling == 0 && isRedstonePowered;
	}

	@Override
	public void burn() {
		LiquidStack fuel = this.fuelTank.getLiquid();
		if (currentFuel == null) {
			currentFuel = IronEngineFuel.getFuelForLiquid(fuel);
		}

		if (currentFuel == null)
			return;

		if (penaltyCooling <= 0 && isRedstonePowered) {

			lastPowered = true;

			if (burnTime > 0 || fuel.amount > 0) {
				if (burnTime > 0) {
					burnTime--;
				}
				if (burnTime <= 0) {
					if (fuel != null) {
						if (--fuel.amount <= 0) {
							fuelTank.setLiquid(null);
						}
						burnTime = currentFuel.totalBurningTime / LiquidContainerRegistry.BUCKET_VOLUME;
					} else {
						currentFuel = null;
						return;
					}
				}
				currentOutput = currentFuel.powerPerCycle; // Comment out for constant power
				addEnergy(currentFuel.powerPerCycle);
				heat += currentFuel.powerPerCycle * HEAT_PER_MJ;
			}
		} else if (penaltyCooling <= 0) {
			if (lastPowered) {
				lastPowered = false;
				penaltyCooling = 30 * 20;
				// 30 sec of penalty on top of the cooling
			}
		}
	}

	@Override
	public void updateHeatLevel() {
	}

	@Override
	public void engineUpdate() {

		final ItemStack stack = getStackInSlot(0);
		if (stack != null) {
			LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(stack);
			if (liquid == null && heat > IDEAL_HEAT) {
				liquid = IronEngineCoolant.getLiquidCoolant(stack);
			}

			if (liquid != null) {
				if (fill(ForgeDirection.UNKNOWN, liquid, false) == liquid.amount) {
					fill(ForgeDirection.UNKNOWN, liquid, true);
					setInventorySlotContents(0, Utils.consumeItem(stack));
				}
			}
		}

		if (heat > IDEAL_HEAT) {
			float extraHeat = heat - IDEAL_HEAT;

			LiquidStack coolant = this.coolantTank.getLiquid();
			Coolant currentCoolant = IronEngineCoolant.getCoolant(coolant);
			if (currentCoolant != null) {
				float cooling = currentCoolant.getDegreesCoolingPerMB(heat);
				if (coolant.amount * cooling > extraHeat) {
					coolant.amount -= Math.round(extraHeat / cooling);
					heat = IDEAL_HEAT;
				} else {
					heat -= coolant.amount * cooling;
					coolantTank.setLiquid(null);
				}
			}
		}

		if (heat > MIN_HEAT && (penaltyCooling > 0 || !isRedstonePowered)) {
			heat -= COOLDOWN_RATE;

		}

		if (heat <= MIN_HEAT) {
			heat = MIN_HEAT;
		}

		if (heat <= MIN_HEAT && penaltyCooling > 0) {
			penaltyCooling--;
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return this.fuelTank.getLiquid() != null ? (int) (((float) this.fuelTank.getLiquid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		fuelTank.readFromNBT(data.getCompoundTag("fuelTank"));
		coolantTank.readFromNBT(data.getCompoundTag("coolantTank"));

		burnTime = data.getInteger("burnTime");
		penaltyCooling = data.getInteger("penaltyCooling");

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("fuelTank", fuelTank.writeToNBT(new NBTTagCompound()));
		data.setTag("coolantTank", coolantTank.writeToNBT(new NBTTagCompound()));

		data.setInteger("burnTime", burnTime);
		data.setInteger("penaltyCooling", penaltyCooling);

	}

	public int getScaledCoolant(int i) {
		return coolantTank.getLiquid() != null ? (int) (((float) coolantTank.getLiquid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}

	@Override
	public void getGUINetworkData(int id, int value) {
		super.getGUINetworkData(id, value);
		switch (id) {
			case 15:
				if (fuelTank.getLiquid() == null) {
					fuelTank.setLiquid(new LiquidStack(0, value));
				} else {
					fuelTank.getLiquid().amount = value;
				}
				break;
			case 16:
				if (fuelTank.getLiquid() == null) {
					fuelTank.setLiquid(new LiquidStack(value, 0));
				} else {
					fuelTank.setLiquid(new LiquidStack(value, fuelTank.getLiquid().amount, fuelTank.getLiquid().itemMeta));
				}
				break;
			case 17:
				if (coolantTank.getLiquid() == null) {
					coolantTank.setLiquid(new LiquidStack(0, value));
				} else {
					coolantTank.getLiquid().amount = value;
				}
				break;
			case 18:
				if (coolantTank.getLiquid() == null) {
					coolantTank.setLiquid(new LiquidStack(value, 0));
				} else {
					coolantTank.setLiquid(new LiquidStack(value, coolantTank.getLiquid().amount, coolantTank.getLiquid().itemMeta));
				}
				break;
			case 19:
				if (fuelTank.getLiquid() == null) {
					fuelTank.setLiquid(new LiquidStack(0, 0, value));
				} else {
					fuelTank.setLiquid(new LiquidStack(fuelTank.getLiquid().itemID, fuelTank.getLiquid().amount, value));
				}
				break;
			case 20:
				if (coolantTank.getLiquid() == null) {
					coolantTank.setLiquid(new LiquidStack(0, 0, value));
				} else {
					coolantTank.setLiquid(new LiquidStack(coolantTank.getLiquid().itemID, coolantTank.getLiquid().amount, value));
				}
		}
	}

	@Override
	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		super.sendGUINetworkData(containerEngine, iCrafting);
		iCrafting.sendProgressBarUpdate(containerEngine, 15, fuelTank.getLiquid() != null ? fuelTank.getLiquid().amount : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 16, fuelTank.getLiquid() != null ? fuelTank.getLiquid().itemID : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 17, coolantTank.getLiquid() != null ? coolantTank.getLiquid().amount : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 18, coolantTank.getLiquid() != null ? coolantTank.getLiquid().itemID : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 19, fuelTank.getLiquid() != null ? fuelTank.getLiquid().itemMeta : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 20, coolantTank.getLiquid() != null ? coolantTank.getLiquid().itemMeta : 0);
	}

	@Override
	public boolean isActive() {
		return penaltyCooling <= 0;
	}

	/* ITANKCONTAINER */
	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {

		// Handle coolant
		if (IronEngineCoolant.getCoolant(resource) != null)
			return coolantTank.fill(resource, doFill);

		if (IronEngineFuel.getFuelForLiquid(resource) != null)
			return fuelTank.fill(resource, doFill);

		return 0;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		switch (direction) {
			case UP:
				return fuelTank;
			case DOWN:
				return coolantTank;
			default:
				return null;
		}
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return new ILiquidTank[]{fuelTank, coolantTank};
	}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		if (itemstack == null)
			return false;
		if (Block.ice.blockID == itemstack.itemID)
			return true;
		return LiquidContainerRegistry.getLiquidForFilledItem(itemstack) != null;
	}

	public LiquidStack getFuel() {
		return fuelTank.getLiquid();
	}

	public LiquidStack getCoolant() {
		return coolantTank.getLiquid();
	}

	@Override
	public float maxEnergyReceived() {
		return 2000;
	}

	@Override
	public float maxEnergyExtracted() {
		return 500;
	}

	@Override
	public float getMaxEnergy() {
		return 10000;
	}

	@Override
	public float getCurrentOutput() {
		if (currentFuel == null) {
			return 0;
		}
		return currentFuel.powerPerCycle;
	}

	@Override
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> triggers = super.getTriggers();
		triggers.add(BuildCraftCore.triggerEmptyLiquid);
		triggers.add(BuildCraftCore.triggerContainsLiquid);
		triggers.add(BuildCraftCore.triggerSpaceLiquid);
		triggers.add(BuildCraftCore.triggerFullLiquid);

		return triggers;
	}
}
