/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

/**
 * In this implementation, the blueprint library is the interface to the
 * *local* player blueprint. The player will be able to load blueprint on his
 * environment, and save blueprints to the server environment.
 */
public class TileBlueprintLibrary extends TileBuildCraft implements IInventory {
	public ItemStack[] stack = new ItemStack[4];

	private static final int PROGRESS_TIME = 100;

	public int progressIn = 0;
	public int progressOut = 0;

	@NetworkData
	public String owner = "";

	public ArrayList<BlueprintId> currentPage;

	public int selected = -1;
	boolean locked = false;

	public EntityPlayer uploadingPlayer = null;
	public EntityPlayer downloadingPlayer = null;

	int pageId = 0;

	public TileBlueprintLibrary() {

	}

	@Override
	public void initialize() {
		super.initialize();

		if (worldObj.isRemote) {
			setCurrentPage(BuildCraftBuilders.clientDB.getPage (0));
		}
	}

	public ArrayList<BlueprintId> getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(ArrayList<BlueprintId> newPage) {
		currentPage = newPage;
		selected = -1;
	}

	public void deleteSelectedBpt() {
		/*BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));
		if (selected > -1 && selected < currentPage.size()) {
			index.deleteBluePrint(currentPage.get(selected).file.getName());
			if (currentPage.size() > 0) {
				currentPage = getNextPage(index.prevBpt(currentPage.get(0).file.getName()));
			} else {
				currentPage = getNextPage(null);
			}
			selected = -1;
			updateCurrentNames();
		}*/
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		owner = nbttagcompound.getString("owner");
		locked = nbttagcompound.getBoolean("locked");

		InvUtils.readStacksFromNBT(nbttagcompound, "stack", stack);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setString("owner", owner);
		nbttagcompound.setBoolean("locked", locked);

		InvUtils.writeStacksToNBT(nbttagcompound, "stack", stack);
	}

	@Override
	public int getSizeInventory() {
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return stack[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (stack[i] == null) {
			return null;
		}

		ItemStack res = stack[i].splitStack(j);

		if (stack[i].stackSize == 0) {
			stack[i] = null;
		}

		return res;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		stack[i] = itemstack;

		if (i == 0) {
			if (stack[0] != null && stack[0].getItem() instanceof ItemBlueprint) {
				progressIn = 1;
			} else {
				progressIn = 0;
			}
		}

		if (i == 2) {
			if (stack[2] != null && stack[2].getItem() instanceof ItemBlueprint) {
				progressOut = 1;
			} else {
				progressOut = 0;
			}
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (stack[slot] == null) {
			return null;
		}

		ItemStack toReturn = stack[slot];
		stack[slot] = null;
		return toReturn;
	}

	@Override
	public String getInventoryName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (progressIn > 0 && progressIn < PROGRESS_TIME) {
			progressIn++;
		}

		if (progressOut > 0 && progressOut < PROGRESS_TIME) {
			progressOut++;
		}

		// On progress IN, we'll download the blueprint from the server to the
		// client, and then store it to the client.
		if (progressIn == 100 && stack[1] == null) {
			setInventorySlotContents(1, stack[0]);
			setInventorySlotContents(0, null);

			BlueprintBase bpt = ItemBlueprint.getBlueprint(stack [1]);

			if (bpt != null && uploadingPlayer != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				bpt.writeToNBT(nbt);
				RPCHandler.rpcPlayer(this, "downloadBlueprintToClient", uploadingPlayer, bpt.id, nbt);
				uploadingPlayer = null;
			}
		}

		if (progressOut == 100 && stack[3] == null) {
			RPCHandler.rpcPlayer(this, "requestSelectedBlueprint",
					downloadingPlayer);
			progressOut = 0;
		}
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@RPC (RPCSide.CLIENT)
	public void requestSelectedBlueprint () {
		if (selected > -1 && selected < currentPage.size()) {
			BlueprintBase bpt = BuildCraftBuilders.clientDB.get(currentPage.get(selected));
			NBTTagCompound nbt = new NBTTagCompound();
			bpt.writeToNBT(nbt);

			RPCHandler.rpcServer(this, "uploadBlueprintToServer", bpt.id, nbt);
		} else {
			RPCHandler.rpcServer(this, "uploadBlueprintToServer", null, null);
		}
	}

	@RPC (RPCSide.SERVER)
	public void uploadBlueprintToServer (BlueprintId id, NBTTagCompound data) {
		if (data != null) {
			BlueprintBase bpt = BlueprintBase.loadBluePrint(data);
			bpt.id = id;
			BuildCraftBuilders.serverDB.add(bpt);
			setInventorySlotContents(3, ItemBlueprint.getBlueprintItem(bpt));
		} else {
			setInventorySlotContents(3, stack [2]);
		}

		setInventorySlotContents(2, null);

		downloadingPlayer = null;
	}

	@RPC (RPCSide.CLIENT)
	public void downloadBlueprintToClient (BlueprintId id, NBTTagCompound data) {
		BlueprintBase bpt = BlueprintBase.loadBluePrint(data);
		bpt.id = id;

		BuildCraftBuilders.clientDB.add(bpt);
		setCurrentPage(BuildCraftBuilders.clientDB.getPage (pageId));
	}

	public void selectBlueprint (int index) {
		selected = index;
	}
}
