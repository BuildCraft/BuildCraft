/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Orientations;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.core.ActionMachineControl;
import buildcraft.core.Box;
import buildcraft.core.IMachine;
import buildcraft.core.ProxyCore;
import buildcraft.core.StackUtil;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.Utils;
import buildcraft.core.ActionMachineControl.Mode;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class TileFiller extends TileBuildCraft implements ISpecialInventory, IPowerReceptor, IMachine, IActionReceptor {

	public @TileNetworkData
	Box box = new Box();
	public @TileNetworkData
	int currentPatternId = 0;
	public @TileNetworkData
	boolean done = true;

	public IFillerPattern currentPattern;

	boolean forceDone = false;
	private ItemStack contents[];
	IPowerProvider powerProvider;

	private ActionMachineControl.Mode lastMode = ActionMachineControl.Mode.Unknown;

	public TileFiller() {
		contents = new ItemStack[getSizeInventory()];
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(10, 25, 100, 25, 100);
		powerProvider.configurePowerPerdition(25, 40);
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!ProxyCore.proxy.isRemote(worldObj)) {
			IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord, zCoord);

			if (a != null) {
				box.initialize(a);

				if (a instanceof TileMarker) {
					((TileMarker) a).removeFromWorld();
				}

				if (!ProxyCore.proxy.isRemote(worldObj) && box.isInitialized()) {
					box.createLasers(worldObj, LaserKind.Stripes);
				}
				sendNetworkUpdate();
			}
		}

		computeRecipe();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (done) {
			if (lastMode == Mode.Loop) {
				done = false;
			} else {
				return;
			}
		}

		if (powerProvider.getEnergyStored() > 25) {
			doWork();
		}
	}

	@Override
	public void doWork() {
		if (ProxyCore.proxy.isRemote(worldObj)) {
			return;
		}

		if (lastMode == Mode.Off) {
			return;
		}

		if (powerProvider.useEnergy(25, 25, true) < 25) {
			return;
		}

		if (box.isInitialized() && currentPattern != null && !done) {
			ItemStack stack = null;
			int stackId = 0;

			for (int s = 9; s < getSizeInventory(); ++s) {
				if (getStackInSlot(s) != null && getStackInSlot(s).stackSize > 0) {

					stack = contents[s];
					stackId = s;

					break;
				}
			}

			done = currentPattern.iteratePattern(this, box, stack);

			if (stack != null && stack.stackSize == 0) {
				contents[stackId] = null;
			}

			if (done) {
				worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
				sendNetworkUpdate();
			}
		}

		if (powerProvider.getEnergyStored() > 25) {
			doWork();
		}
	}

	@Override
	public int getSizeInventory() {
		return 36;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return contents[i];
	}

	public void computeRecipe() {
		if (ProxyCore.proxy.isRemote(worldObj)) {
			return;
		}

		IFillerPattern newPattern = FillerManager.registry.findMatchingRecipe(this);

		if (newPattern == currentPattern) {
			return;
		}

		currentPattern = newPattern;

		if (currentPattern == null || forceDone) {
			done = lastMode != Mode.Loop;
			forceDone = false;
		} else {
			done = false;
		}

		if (worldObj != null) {
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		}

		if (currentPattern == null) {
			currentPatternId = 0;
		} else {
			currentPatternId = currentPattern.getId();
		}

		if (ProxyCore.proxy.isSimulating(worldObj)) {
			sendNetworkUpdate();
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (contents[i] != null) {
			if (contents[i].stackSize <= j) {
				ItemStack itemstack = contents[i];
				contents[i] = null;
				// onInventoryChanged();

				computeRecipe();

				return itemstack;
			}

			ItemStack itemstack1 = contents[i].splitStack(j);

			if (contents[i].stackSize == 0) {
				contents[i] = null;
			}
			// onInventoryChanged();

			computeRecipe();

			return itemstack1;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		contents[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}

		computeRecipe();
		// onInventoryChanged();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (contents[slot] == null)
			return null;
		ItemStack toReturn = contents[slot];
		contents[slot] = null;
		return toReturn;
	}

	@Override
	public String getInvName() {
		return "Filler";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		Utils.readStacksFromNBT(nbttagcompound, "Items", contents);

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		done = nbttagcompound.getBoolean("done");
		lastMode = Mode.values()[nbttagcompound.getByte("lastMode")];

		forceDone = done;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		Utils.writeStacksToNBT(nbttagcompound, "Items", contents);

		if (box != null) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}

		nbttagcompound.setBoolean("done", done);
		nbttagcompound.setByte("lastMode", (byte) lastMode.ordinal());
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}
		return entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		if (box != null) {
			box.deleteLasers();
		}
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();

		super.handleDescriptionPacket(packet);

		currentPattern = FillerManager.registry.getPattern(currentPatternId);
		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);

		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();

		super.handleUpdatePacket(packet);

		currentPattern = FillerManager.registry.getPattern(currentPatternId);
		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);

		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public boolean isActive() {
		return !done && lastMode != Mode.Off;
	}

	@Override
	public boolean manageLiquids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public void openChest() {

	}

	@Override
	public void closeChest() {

	}

	@Override
	public int powerRequest() {
		if (isActive()) {
			return powerProvider.getMaxEnergyReceived();
		} else {
			return 0;
		}
	}

	@Override
	public void actionActivated(IAction action) {
		if (action == BuildCraftCore.actionOn) {
			lastMode = ActionMachineControl.Mode.On;
		} else if (action == BuildCraftCore.actionOff) {
			lastMode = ActionMachineControl.Mode.Off;
		} else if (action == BuildCraftCore.actionLoop) {
			lastMode = ActionMachineControl.Mode.Loop;
		}
	}

	@Override
	public boolean allowActions() {
		return true;
	}

	/* ISPECIALINVENTORY */
	@Override
	public int addItem(ItemStack stack, boolean doAdd, Orientations from) {
		StackUtil stackUtil = new StackUtil(stack);

		boolean added = false;

		for (int i = 9; i < contents.length; ++i) {
			if (stackUtil.tryAdding(this, i, doAdd, false)) {
				added = true;
				break;
			}
		}

		if (added) {
			if (!doAdd) {
				return stackUtil.itemsAdded;
			} else if (stack.stackSize - stackUtil.itemsAdded <= 0) {
				return stackUtil.itemsAdded;
			} else {
				addItem(stack, added, from);

				return stackUtil.itemsAdded;
			}
		}

		if (!added) {
			for (int i = 9; i < contents.length; ++i) {
				if (stackUtil.tryAdding(this, i, doAdd, true)) {
					added = true;
					break;
				}
			}
		}

		if (added) {
			if (!doAdd) {
				return stackUtil.itemsAdded;
			} else if (stack.stackSize - stackUtil.itemsAdded <= 0) {
				return stackUtil.itemsAdded;
			} else {
				addItem(stack, added, from);

				return stackUtil.itemsAdded;
			}
		}

		return 0;
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, Orientations from, int maxItemCount) {
		for (int i = 9; i < contents.length; ++i) {
			if (contents[i] != null) {
				if (doRemove) {
					return new ItemStack[] { decrStackSize(i, 1) };
				} else {
					return new ItemStack[] { contents[i] };
				}
			}
		}

		return null;
	}

}
