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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineCoolant.Coolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.fuels.IronEngineFuel.Fuel;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.Tank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.energy.gui.ContainerEngine;

public class TileEngineIron extends TileEngineWithInventory implements IFluidHandler {

	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 10;
	public static float HEAT_PER_MJ = 0.0023F;
	public static float COOLDOWN_RATE = 0.05F;
	public static int MAX_COOLANT_PER_TICK = 40;
	int burnTime = 0;
	public Tank tankFuel = new Tank("tankFuel", MAX_LIQUID, this);
	public Tank tankCoolant = new Tank("tankCoolant", MAX_LIQUID, this);
	private TankManager tankManager = new TankManager();
	private Fuel currentFuel = null;
	public int penaltyCooling = 0;
	boolean lastPowered = false;
	private BiomeGenBase biomeCache;

	public TileEngineIron() {
		super(1);
		tankManager.add(tankFuel);
		tankManager.add(tankCoolant);
	}

	@Override
	public ResourceLocation getTextureFile() {
		return IRON_TEXTURE;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (player.getCurrentEquippedItem() != null) {
			if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
			ItemStack current = player.getCurrentEquippedItem();
			if (current != null) {
				if (CoreProxy.proxy.isSimulating(worldObj)) {
					if (FluidUtils.handleRightClick(this, side, player, true, true)) {
						return true;
					}
				} else {
					if (FluidContainerRegistry.isContainer(current)) {
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

	private float getBiomeTempScalar() {
		if (biomeCache == null)
			biomeCache = worldObj.getBiomeGenForCoords(xCoord, zCoord);
		float tempScalar = biomeCache.temperature - 1.0F;
		tempScalar *= 0.5F;
		tempScalar += 1.0F;
		return tempScalar;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		biomeCache = null;
	}

	@Override
	public boolean isBurning() {
		FluidStack fuel = tankFuel.getFluid();
		return fuel != null && fuel.amount > 0 && penaltyCooling == 0 && isRedstonePowered;
	}

	@Override
	public void burn() {
		FluidStack fuel = this.tankFuel.getFluid();
		if (currentFuel == null && fuel != null) {
			currentFuel = IronEngineFuel.getFuelForFluid(fuel.getFluid());
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
							tankFuel.setFluid(null);
						}
						burnTime = currentFuel.totalBurningTime / FluidContainerRegistry.BUCKET_VOLUME;
					} else {
						currentFuel = null;
						return;
					}
				}
				currentOutput = currentFuel.powerPerCycle; // Comment out for constant power
				addEnergy(currentFuel.powerPerCycle);
				heat += currentFuel.powerPerCycle * HEAT_PER_MJ * getBiomeTempScalar();
			}
		} else if (penaltyCooling <= 0) {
			if (lastPowered) {
				lastPowered = false;
				penaltyCooling = 10;
				// 10 tick of penalty on top of the cooling
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
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(stack);
			if (liquid == null && heat > MIN_HEAT * 2) {
				liquid = IronEngineCoolant.getFluidCoolant(stack);
			}

			if (liquid != null) {
				if (fill(ForgeDirection.UNKNOWN, liquid, false) == liquid.amount) {
					fill(ForgeDirection.UNKNOWN, liquid, true);
					setInventorySlotContents(0, Utils.consumeItem(stack));
				}
			}
		}

		if (heat > MIN_HEAT && (penaltyCooling > 0 || !isRedstonePowered)) {
			heat -= COOLDOWN_RATE;
			coolEngine(MIN_HEAT);
			getEnergyStage();
		} else if (heat > IDEAL_HEAT) {
			coolEngine(IDEAL_HEAT);
		}

		if (heat <= MIN_HEAT && penaltyCooling > 0)
			penaltyCooling--;

		if (heat <= MIN_HEAT)
			heat = MIN_HEAT;
	}

	private void coolEngine(float idealHeat) {
		float extraHeat = heat - idealHeat;

		FluidStack coolant = this.tankCoolant.getFluid();
		if (coolant == null)
			return;

		int coolantAmount = Math.min(MAX_COOLANT_PER_TICK, coolant.amount);
		Coolant currentCoolant = IronEngineCoolant.getCoolant(coolant);
		if (currentCoolant != null) {
			float cooling = currentCoolant.getDegreesCoolingPerMB(heat);
			cooling /= getBiomeTempScalar();
			if (coolantAmount * cooling > extraHeat) {
				tankCoolant.drain(Math.round(extraHeat / cooling), true);
				heat -= extraHeat;
			} else {
				tankCoolant.drain(coolantAmount, true);
				heat -= coolantAmount * cooling;
			}
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return this.tankFuel.getFluid() != null ? (int) (((float) this.tankFuel.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tankManager.readFromNBT(data);

		burnTime = data.getInteger("burnTime");
		penaltyCooling = data.getInteger("penaltyCooling");

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tankManager.writeToNBT(data);

		data.setInteger("burnTime", burnTime);
		data.setInteger("penaltyCooling", penaltyCooling);

	}

	public int getScaledCoolant(int i) {
		return tankCoolant.getFluid() != null ? (int) (((float) tankCoolant.getFluid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}

	@Override
	public void getGUINetworkData(int id, int value) {
		super.getGUINetworkData(id, value);
		switch (id) {
			// Fluid Fuel ID
			case 15:
				if (tankFuel.getFluid() == null) {
					tankFuel.setFluid(new FluidStack(value, 0));
				} else {
					tankFuel.getFluid().fluidID = value;
				}
				break;
			// Fluid Coolant ID
			case 16:
				if (tankCoolant.getFluid() == null) {
					tankCoolant.setFluid(new FluidStack(value, 0));
				} else {
					tankCoolant.getFluid().fluidID = value;
				}
				break;
			// Fluid Fuel amount
			case 17:
				if (tankFuel.getFluid() == null) {
					tankFuel.setFluid(new FluidStack(0, value));
				} else {
					tankFuel.getFluid().amount = value;
				}
				break;
			// Fluid Coolant amount
			case 18:
				if (tankCoolant.getFluid() == null) {
					tankCoolant.setFluid(new FluidStack(0, value));
				} else {
					tankCoolant.getFluid().amount = value;
				}
				break;
			//Fluid Fuel color
			case 19:
				tankFuel.colorRenderCache = value;
				break;
			//Fluid Coolant color
			case 20:
				tankCoolant.colorRenderCache = value;
				break;
		}
	}

	@Override
	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		super.sendGUINetworkData(containerEngine, iCrafting);
		iCrafting.sendProgressBarUpdate(containerEngine, 15, tankFuel.getFluid() != null ? tankFuel.getFluid().fluidID : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 16, tankCoolant.getFluid() != null ? tankCoolant.getFluid().fluidID : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 17, tankFuel.getFluid() != null ? tankFuel.getFluid().amount : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 18, tankCoolant.getFluid() != null ? tankCoolant.getFluid().amount : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 19, tankFuel.colorRenderCache);
		iCrafting.sendProgressBarUpdate(containerEngine, 20, tankCoolant.colorRenderCache);
	}

	@Override
	public boolean isActive() {
		return penaltyCooling <= 0;
	}

	/* ITANKCONTAINER */
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tankFuel.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null)
			return null;
		if (tankCoolant.getFluidType() == resource.getFluid())
			return tankCoolant.drain(resource.amount, doDrain);
		if (tankFuel.getFluidType() == resource.getFluid())
			return tankFuel.drain(resource.amount, doDrain);
		return null;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {

		// Handle coolant
		if (IronEngineCoolant.getCoolant(resource) != null)
			return tankCoolant.fill(resource, doFill);

		if (IronEngineFuel.getFuelForFluid(resource.getFluid()) != null)
			return tankFuel.fill(resource, doFill);

		return 0;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if (IronEngineCoolant.isCoolant(fluid))
			return true;

		if (IronEngineFuel.getFuelForFluid(fluid) != null)
			return true;

		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction) {
		return tankManager.getTankInfo(direction);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if (itemstack == null)
			return false;
		if (IronEngineCoolant.getCoolant(itemstack) != null)
			return true;
		return FluidContainerRegistry.getFluidForFilledItem(itemstack) != null;
	}

	public FluidStack getFuel() {
		return tankFuel.getFluid();
	}

	public FluidStack getCoolant() {
		return tankCoolant.getFluid();
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
		triggers.add(BuildCraftCore.triggerEmptyFluid);
		triggers.add(BuildCraftCore.triggerContainsFluid);
		triggers.add(BuildCraftCore.triggerSpaceFluid);
		triggers.add(BuildCraftCore.triggerFullFluid);

		return triggers;
	}
}
