/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.SingleUseTank;
import buildcraft.core.lib.fluids.TankManager;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.recipes.RefineryRecipeManager;

public class TileRefinery extends TileBuildCraft implements IFluidHandler, IHasWork, IFlexibleCrafter, ICommandReceiver {

	public static int LIQUID_PER_SLOT = FluidContainerRegistry.BUCKET_VOLUME * 4;

	public IFlexibleRecipe<FluidStack> currentRecipe;
	public CraftingResult<FluidStack> craftingResult;

	public SingleUseTank[] tanks = {new SingleUseTank("tank1", LIQUID_PER_SLOT, this),
			new SingleUseTank("tank2", LIQUID_PER_SLOT, this)};

	public SingleUseTank result = new SingleUseTank("result", LIQUID_PER_SLOT, this);
	public TankManager<SingleUseTank> tankManager = new TankManager<SingleUseTank>(tanks[0], tanks[1], result);
	public float animationSpeed = 1;
	private short animationStage = 0;
	private SafeTimeTracker time = new SafeTimeTracker();

	private SafeTimeTracker updateNetworkTime = new SafeTimeTracker(BuildCraftCore.updateFactor);
	private boolean isActive;

	private String currentRecipeId = "";

	public TileRefinery() {
		super();
		this.setBattery(new RFBattery(10000, 1500, 0));
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			simpleAnimationIterate();
			return;
		}

		if (updateNetworkTime.markTimeIfDelay(worldObj)) {
			sendNetworkUpdate();
		}

		isActive = false;

		if (currentRecipe == null) {
			decreaseAnimation();
			return;
		}

		if (result.fill(craftingResult.crafted.copy(), false) != craftingResult.crafted.amount) {
			decreaseAnimation();
			return;
		}

		isActive = true;

		if (getBattery().getEnergyStored() >= craftingResult.energyCost) {
			increaseAnimation();
		} else {
			decreaseAnimation();
		}

		if (!time.markTimeIfDelay(worldObj, craftingResult.craftingTime)) {
			return;
		}

		if (getBattery().useEnergy(craftingResult.energyCost, craftingResult.energyCost, true) > 0) {
			CraftingResult<FluidStack> r = currentRecipe.craft(this, true);
			if (r != null && r.crafted != null) {
				getBattery().useEnergy(craftingResult.energyCost, craftingResult.energyCost, false);
				r = currentRecipe.craft(this, false);
				if (r != null && r.crafted != null) {
					// Shouldn't really happen, but its not properly documented
					result.fill(r.crafted.copy(), true);
				}
			}
		}
	}

	@Override
	public boolean hasWork() {
		return isActive;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		tankManager.readFromNBT(data);

		animationStage = data.getShort("animationStage");
		animationSpeed = data.getFloat("animationSpeed");

		updateRecipe();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		tankManager.writeToNBT(data);

		data.setShort("animationStage", animationStage);
		data.setFloat("animationSpeed", animationSpeed);
	}

	public int getAnimationStage() {
		return animationStage;
	}

	/**
	 * Used to iterate the animation without computing the speed
	 */
	public void simpleAnimationIterate() {
		if (animationSpeed > 1) {
			animationStage += animationSpeed;

			if (animationStage > 300) {
				animationStage = 100;
			}
		} else if (animationStage > 0) {
			animationStage--;
		}
	}

	public void increaseAnimation() {
		if (animationSpeed < 2) {
			animationSpeed = 2;
		} else if (animationSpeed <= 5) {
			animationSpeed += 0.1;
		}

		animationStage += animationSpeed;

		if (animationStage > 300) {
			animationStage = 100;
		}
	}

	public void decreaseAnimation() {
		if (animationSpeed >= 1) {
			animationSpeed -= 0.1;

			animationStage += animationSpeed;

			if (animationStage > 300) {
				animationStage = 100;
			}
		} else {
			if (animationStage > 0) {
				animationStage--;
			}
		}
	}

	public void resetFilters() {
		for (SingleUseTank tank : tankManager) {
			tank.setAcceptedFluid(null);
		}
	}

	public void setFilter(int number, Fluid fluid) {
		tankManager.get(number).setAcceptedFluid(fluid);
	}

	public Fluid getFilter(int number) {
		return tankManager.get(number).getAcceptedFluid();
	}

	/* SMP GUI */
	public void getGUINetworkData(int id, int data) {
		switch (id) {
			case 0:
				setFilter(0, FluidRegistry.getFluid(data));
				break;
			case 1:
				setFilter(1, FluidRegistry.getFluid(data));
				break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		if (getFilter(0) != null) {
			iCrafting.sendProgressBarUpdate(container, 0, getFilter(0).getID());
		}
		if (getFilter(1) != null) {
			iCrafting.sendProgressBarUpdate(container, 1, getFilter(1).getID());
		}
	}

	/* ITANKCONTAINER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		int used = 0;
		FluidStack resourceUsing = resource.copy();

		if (RefineryRecipeManager.INSTANCE.getValidFluidStacks1().contains(resource)) {
			used += tanks[0].fill(resourceUsing, doFill);
			resourceUsing.amount -= used;
		}
		if (RefineryRecipeManager.INSTANCE.getValidFluidStacks2().contains(resource)) {
			used += tanks[1].fill(resourceUsing, doFill);
			resourceUsing.amount -= used;
		}
		updateRecipe();

		return used;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxEmpty, boolean doDrain) {
		FluidStack r = result.drain(maxEmpty, doDrain);

		updateRecipe();

		return r;
	}

	private void updateRecipe() {
		currentRecipe = null;
		craftingResult = null;

		for (IFlexibleRecipe<FluidStack> recipe : RefineryRecipeManager.INSTANCE.getRecipes()) {
			craftingResult = recipe.craft(this, true);

			if (craftingResult != null) {
				currentRecipe = recipe;
				currentRecipeId = currentRecipe.getId();
				break;
			}
		}
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null || !resource.isFluidEqual(result.getFluid())) {
			return null;
		}
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction) {
		return tankManager.getTankInfo(direction);
	}

	// Network
	public void writeData(ByteBuf stream) {
		stream.writeFloat(animationSpeed);
		NetworkUtils.writeUTF(stream, currentRecipeId);
		tankManager.writeData(stream);
	}

	@Override
	public void readData(ByteBuf stream) {
		animationSpeed = stream.readFloat();
		currentRecipeId = NetworkUtils.readUTF(stream);
		tankManager.readData(stream);

		currentRecipe = RefineryRecipeManager.INSTANCE.getRecipe(currentRecipeId);

		if (currentRecipe != null) {
			craftingResult = currentRecipe.craft(this, true);
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public int getCraftingItemStackSize() {
		return 0;
	}

	@Override
	public ItemStack getCraftingItemStack(int slotid) {
		return null;
	}

	@Override
	public ItemStack decrCraftingItemStack(int slotid, int val) {
		return null;
	}

	@Override
	public FluidStack getCraftingFluidStack(int tankid) {
		return tanks[tankid].getFluid();
	}

	@Override
	public FluidStack decrCraftingFluidStack(int tankid, int val) {
		FluidStack resultF;

		if (val >= tanks[tankid].getFluid().amount) {
			resultF = tanks[tankid].getFluid();
			tanks[tankid].setFluid(null);
		} else {
			resultF = tanks[tankid].getFluid().copy();
			resultF.amount = val;
			tanks[tankid].getFluid().amount -= val;
		}

		updateRecipe();

		return resultF;
	}

	@Override
	public int getCraftingFluidStackSize() {
		return tanks.length;
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side == Side.SERVER && "setFilter".equals(command)) {
			setFilter(stream.readByte(), FluidRegistry.getFluid(stream.readShort()));
		}
	}
}
