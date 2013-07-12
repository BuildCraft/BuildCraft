/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.recipes.RefineryRecipe;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class TileRefinery extends TileBuildCraft implements IFluidHandler, IPowerReceptor, IInventory, IMachine {

	private int[] filters = new int[2];
	private int[] filtersMeta = new int[2];
	public static int LIQUID_PER_SLOT = FluidContainerRegistry.BUCKET_VOLUME * 4;
	public FluidTank ingredient1 = new FluidTank(LIQUID_PER_SLOT);
	public FluidTank ingredient2 = new FluidTank(LIQUID_PER_SLOT);
	public FluidTank result = new FluidTank(LIQUID_PER_SLOT);
	public float animationSpeed = 1;
	private int animationStage = 0;
	SafeTimeTracker time = new SafeTimeTracker();
	SafeTimeTracker updateNetworkTime = new SafeTimeTracker();
	private PowerHandler powerHandler;
	private boolean isActive;

	public TileRefinery() {
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();

		filters[0] = 0;
		filters[1] = 0;
		filtersMeta[0] = 0;
		filtersMeta[1] = 0;
	}

	private void initPowerProvider() {
		powerHandler.configure(25, 100, 25, 1000);
		powerHandler.configurePowerPerdition(1, 1);
	}

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
	public String getInvName() {
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
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public void updateEntity() {
		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			simpleAnimationIterate();
			return;

		} else if (CoreProxy.proxy.isSimulating(worldObj) && updateNetworkTime.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
			sendNetworkUpdate();
		}

		isActive = false;

		RefineryRecipe currentRecipe = null;

		currentRecipe = RefineryRecipe.findRefineryRecipe(ingredient1.getFluid().getFluid(), ingredient2.getFluid().getFluid());

		FluidStack recipeStack = new FluidStack(currentRecipe.result, amount);
		if (currentRecipe == null) {
			decreaseAnimation();
			return;
		}

		if (result.getFluid() != null && result.getFluid().amount != 0 && !result.getFluid().getFluid().equals(currentRecipe.result)) {
			decreaseAnimation();
			return;
		}

		if (result.fill(currentRecipe.result, false) != currentRecipe.result.amount) {
			decreaseAnimation();
			return;
		}

		if (!containsInput(currentRecipe.ingredient1) || !containsInput(currentRecipe.ingredient2)) {
			decreaseAnimation();
			return;
		}

		isActive = true;

		if (powerHandler.getEnergyStored() >= currentRecipe.energy) {
			increaseAnimation();
		} else {
			decreaseAnimation();
		}

		if (!time.markTimeIfDelay(worldObj, currentRecipe.delay))
			return;

		float energyUsed = powerHandler.useEnergy(currentRecipe.energy, currentRecipe.energy, true);

		if (energyUsed != 0) {
			if (consumeInput(currentRecipe.ingredient1) && consumeInput(currentRecipe.ingredient2)) {
				result.fill(currentRecipe.result, true);
			}
		}
	}

	private boolean containsInput(FluidStack liquid) {
		if (liquid == null)
			return true;

		return (ingredient1.getFluid() != null && ingredient1.getFluid().containsFluid(liquid))
				|| (ingredient2.getFluid() != null && ingredient2.getFluid().containsFluid(liquid));
	}

	private boolean consumeInput(FluidStack liquid) {
		if (liquid == null)
			return true;

		if (ingredient1.getFluid() != null && ingredient1.getFluid().containsFluid(liquid)) {
			ingredient1.drain(liquid.amount, true);
			return true;
		} else if (ingredient2.getFluid() != null && ingredient2.getFluid().containsFluid(liquid)) {
			ingredient2.drain(liquid.amount, true);
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

	// for compatibility
	private FluidStack readSlotNBT(NBTTagCompound nbttagcompound) {
		int liquidId = nbttagcompound.getInteger("liquidId");
		int quantity = 0;
		int liquidMeta = 0;

		if (liquidId != 0) {
			quantity = nbttagcompound.getInteger("quantity");
			liquidMeta = nbttagcompound.getInteger("liquidMeta");
		} else {
			quantity = 0;
		}

		if (quantity > 0)
			return new FluidStack(liquidId, quantity, liquidMeta);

		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("slot1")) {
			ingredient1.setFluid(readSlotNBT(nbttagcompound.getCompoundTag("slot1")));
			ingredient2.setFluid(readSlotNBT(nbttagcompound.getCompoundTag("slot2")));
			result.setFluid(readSlotNBT(nbttagcompound.getCompoundTag("result")));
		} else {
			if (nbttagcompound.hasKey("ingredient1")) {
				ingredient1.setFluid(FluidStack.loadFluidStackFromNBT(nbttagcompound.getCompoundTag("ingredient1")));
			}
			if (nbttagcompound.hasKey("ingredient2")) {
				ingredient2.setFluid(FluidStack.loadFluidStackFromNBT(nbttagcompound.getCompoundTag("ingredient2")));
			}
			if (nbttagcompound.hasKey("result")) {
				result.setFluid(FluidStack.loadFluidStackFromNBT(nbttagcompound.getCompoundTag("result")));
			}
		}

		animationStage = nbttagcompound.getInteger("animationStage");
		animationSpeed = nbttagcompound.getFloat("animationSpeed");

		powerHandler.readFromNBT(nbttagcompound);
		initPowerProvider();

		filters[0] = nbttagcompound.getInteger("filters_0");
		filters[1] = nbttagcompound.getInteger("filters_1");
		filtersMeta[0] = nbttagcompound.getInteger("filtersMeta_0");
		filtersMeta[1] = nbttagcompound.getInteger("filtersMeta_1");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (ingredient1.getFluid() != null) {
			nbttagcompound.setTag("ingredient1", ingredient1.getFluid().writeToNBT(new NBTTagCompound()));
		}

		if (ingredient2.getFluid() != null) {
			nbttagcompound.setTag("ingredient2", ingredient2.getFluid().writeToNBT(new NBTTagCompound()));
		}

		if (result.getFluid() != null) {
			nbttagcompound.setTag("result", result.getFluid().writeToNBT(new NBTTagCompound()));
		}

		nbttagcompound.setInteger("animationStage", animationStage);
		nbttagcompound.setFloat("animationSpeed", animationSpeed);
		powerHandler.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("filters_0", filters[0]);
		nbttagcompound.setInteger("filters_1", filters[1]);
		nbttagcompound.setInteger("filtersMeta_0", filtersMeta[0]);
		nbttagcompound.setInteger("filtersMeta_1", filtersMeta[1]);
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
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	public void setFilter(int number, int liquidId, int liquidMeta) {
		filters[number] = liquidId;
		filtersMeta[number] = liquidMeta;
	}

	public int getFilter(int number) {
		return filters[number];
	}

	public int getFilterMeta(int number) {
		return filtersMeta[number];
	}

	@Override
	public boolean allowAction(IAction action) {
		return false;
	}

	/* SMP GUI */
	public void getGUINetworkData(int i, int j) {
		switch (i) {
			case 0:
				filters[0] = j;
				break;
			case 1:
				filters[1] = j;
				break;
			case 2:
				filtersMeta[0] = j;
				break;
			case 3:
				filtersMeta[1] = j;
				break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		iCrafting.sendProgressBarUpdate(container, 0, filters[0]);
		iCrafting.sendProgressBarUpdate(container, 1, filters[1]);
		iCrafting.sendProgressBarUpdate(container, 2, filtersMeta[0]);
		iCrafting.sendProgressBarUpdate(container, 3, filtersMeta[1]);
	}

	/* ITANKCONTAINER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		int used = 0;
		FluidStack resourceUsing = resource.copy();

		if (filters[0] != 0 || filters[1] != 0) {
			if (filters[0] == resource.itemID && filtersMeta[0] == resource.itemMeta) {
				used += ingredient1.fill(resourceUsing, doFill);
			}

			resourceUsing.amount -= used;

			if (filters[1] == resource.itemID && filtersMeta[1] == resource.itemMeta) {
				used += ingredient2.fill(resourceUsing, doFill);
			}
		} else {
			used += ingredient1.fill(resourceUsing, doFill);
			resourceUsing.amount -= used;
			used += ingredient2.fill(resourceUsing, doFill);
		}

		if (doFill && used > 0) {
			updateNetworkTime.markTime(worldObj);
			sendNetworkUpdate();
		}

		return used;
	}

	@Override
	public int fill(int tankIndex, FluidStack resource, boolean doFill) {

		if (tankIndex == 0 && resource.itemID == filters[0] && resource.itemMeta == filtersMeta[0])
			return ingredient1.fill(resource, doFill);
		if (tankIndex == 1 && resource.itemID == filters[1] && resource.itemMeta == filtersMeta[1])
			return ingredient2.fill(resource, doFill);
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxEmpty, boolean doDrain) {
		return drain(2, maxEmpty, doDrain);
	}

	@Override
	public FluidStack drain(int tankIndex, int maxEmpty, boolean doDrain) {
		if (tankIndex == 2)
			return result.drain(maxEmpty, doDrain);

		return null;
	}

	@Override
	public IFluidTank[] getTanks(ForgeDirection direction) {
		return new IFluidTank[]{ingredient1, ingredient2, result};
	}

	@Override
	public IFluidTank getTank(ForgeDirection direction, FluidStack type) {
		ForgeDirection dir = ForgeDirection.getOrientation(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));

		switch (direction) {
			case NORTH:
				switch (dir) {
					case WEST:
						return ingredient2;
					case EAST:
						return ingredient1;
					default:
						return null;
				}
			case SOUTH:
				switch (dir) {
					case WEST:
						return ingredient1;
					case EAST:
						return ingredient2;
					default:
						return null;
				}
			case EAST:
				switch (dir) {
					case NORTH:
						return ingredient2;
					case SOUTH:
						return ingredient1;
					default:
						return null;
				}
			case WEST:
				switch (dir) {
					case NORTH:
						return ingredient1;
					case SOUTH:
						return ingredient2;
					default:
						return null;
				}
			case DOWN:
				return result;
			default:
				return null;
		}
	}

	// Network
	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(9, 1, 0);
		if (ingredient1.getFluid() != null) {
			payload.intPayload[0] = ingredient1.getFluid().itemID;
			payload.intPayload[1] = ingredient1.getFluid().itemMeta;
			payload.intPayload[2] = ingredient1.getFluid().amount;
		} else {
			payload.intPayload[0] = 0;
			payload.intPayload[1] = 0;
			payload.intPayload[2] = 0;
		}
		if (ingredient2.getFluid() != null) {
			payload.intPayload[3] = ingredient2.getFluid().itemID;
			payload.intPayload[4] = ingredient2.getFluid().itemMeta;
			payload.intPayload[5] = ingredient2.getFluid().amount;
		} else {
			payload.intPayload[3] = 0;
			payload.intPayload[4] = 0;
			payload.intPayload[5] = 0;
		}
		if (result.getFluid() != null) {
			payload.intPayload[6] = result.getFluid().itemID;
			payload.intPayload[7] = result.getFluid().itemMeta;
			payload.intPayload[8] = result.getFluid().amount;
		} else {
			payload.intPayload[6] = 0;
			payload.intPayload[7] = 0;
			payload.intPayload[8] = 0;
		}
		payload.floatPayload[0] = animationSpeed;

		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		if (packet.payload.intPayload[0] > 0) {
			ingredient1.setFluid(new FluidStack(packet.payload.intPayload[0], packet.payload.intPayload[2], packet.payload.intPayload[1]));
		} else {
			ingredient1.setFluid(null);
		}

		if (packet.payload.intPayload[3] > 0) {
			ingredient2.setFluid(new FluidStack(packet.payload.intPayload[3], packet.payload.intPayload[5], packet.payload.intPayload[4]));
		} else {
			ingredient2.setFluid(null);
		}

		if (packet.payload.intPayload[6] > 0) {
			result.setFluid(new FluidStack(packet.payload.intPayload[6], packet.payload.intPayload[8], packet.payload.intPayload[7]));
		} else {
			result.setFluid(null);
		}

		animationSpeed = packet.payload.floatPayload[0];
	}
}
