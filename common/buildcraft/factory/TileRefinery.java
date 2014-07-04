/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.IAction;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.fluids.TankManager;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager.RefineryRecipe;

public class TileRefinery extends TileBuildCraft implements IFluidHandler, IInventory, IMachine {

	public static int LIQUID_PER_SLOT = FluidContainerRegistry.BUCKET_VOLUME * 4;
	public SingleUseTank tank1 = new SingleUseTank("tank1", LIQUID_PER_SLOT, this);
	public SingleUseTank tank2 = new SingleUseTank("tank2", LIQUID_PER_SLOT, this);
	public SingleUseTank result = new SingleUseTank("result", LIQUID_PER_SLOT, this);
	public TankManager<SingleUseTank> tankManager = new TankManager<SingleUseTank>(tank1, tank2, result);
	public float animationSpeed = 1;
	private int animationStage = 0;
	private SafeTimeTracker time = null;
	private SafeTimeTracker updateNetworkTime = new SafeTimeTracker(BuildCraftCore.updateFactor);
	private boolean isActive;

	@MjBattery(maxCapacity = 1000, maxReceivedPerCycle = 150, minimumConsumption = 1)
	private double mjStored = 0;

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
	}

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) {
			simpleAnimationIterate();
			return;
		}

		if (updateNetworkTime.markTimeIfDelay(worldObj)) {
			sendNetworkUpdate();
		}

		isActive = false;

		RefineryRecipe currentRecipe = RefineryRecipeManager.INSTANCE.findRefineryRecipe(tank1.getFluid(), tank2.getFluid());

		if (currentRecipe == null) {
			decreaseAnimation();
			return;
		}

		if (result.fill(currentRecipe.result.copy(), false) != currentRecipe.result.amount) {
			decreaseAnimation();
			return;
		}

		if (!containsInput(currentRecipe.ingredient1) || !containsInput(currentRecipe.ingredient2)) {
			decreaseAnimation();
			return;
		}

		isActive = true;

		if (mjStored >= currentRecipe.energyCost) {
			increaseAnimation();
		} else {
			decreaseAnimation();
		}

		if (time == null) {
			time = new SafeTimeTracker(currentRecipe.timeRequired);
		}

		if (!time.markTimeIfDelay(worldObj)) {
			return;
		}

        time = null;

		if (mjStored >= currentRecipe.energyCost) {
			mjStored -= currentRecipe.energyCost;

			if (consumeInput(currentRecipe.ingredient1) && consumeInput(currentRecipe.ingredient2)) {
				result.fill(currentRecipe.result, true);
			}
		}
	}

	private boolean containsInput(FluidStack ingredient) {
		if (ingredient == null) {
			return true;
		}

		return (tank1.getFluid() != null && tank1.getFluid().containsFluid(ingredient))
				|| (tank2.getFluid() != null && tank2.getFluid().containsFluid(ingredient));
	}

	private boolean consumeInput(FluidStack liquid) {
		if (liquid == null) {
			return true;
		}

		if (tank1.getFluid() != null && tank1.getFluid().containsFluid(liquid)) {
			tank1.drain(liquid.amount, true);
			return true;
		} else if (tank2.getFluid() != null && tank2.getFluid().containsFluid(liquid)) {
			tank2.drain(liquid.amount, true);
			return true;
		}

		return false;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean manageFluids() {
		return true;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		tankManager.readFromNBT(data);

		animationStage = data.getInteger("animationStage");
		animationSpeed = data.getFloat("animationSpeed");

		mjStored = data.getDouble("mjStored");
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		tankManager.writeToNBT(data);

		data.setInteger("animationStage", animationStage);
		data.setFloat("animationSpeed", animationSpeed);

		data.setDouble("mjStored", mjStored);
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

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
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

	@Override
	public boolean allowAction(IAction action) {
		return false;
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

		used += tank1.fill(resourceUsing, doFill);
		resourceUsing.amount -= used;
		used += tank2.fill(resourceUsing, doFill);

		return used;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxEmpty, boolean doDrain) {
		return result.drain(maxEmpty, doDrain);
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
	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(new PacketPayload.StreamWriter() {
			@Override
			public void writeData(ByteBuf data) {
				data.writeFloat(animationSpeed);
				tankManager.writeData(data);
			}
		});
		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		ByteBuf stream = packet.payload.stream;
		animationSpeed = stream.readFloat();
		tankManager.readData(stream);
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
	public boolean hasCustomInventoryName() {
		return false;
	}
}
