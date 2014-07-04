/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.builders.blueprints.BlueprintId.Kind;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

/**
 * In this implementation, the blueprint library is the interface to the
 * *local* player blueprint. The player will be able to load blueprint on his
 * environment, and save blueprints to the server environment.
 */
public class TileBlueprintLibrary extends TileBuildCraft implements IInventory {
	private static final int PROGRESS_TIME = 100;

	public SimpleInventory inv = new SimpleInventory(4, "Blueprint Library", 1);

	public int progressIn = 0;
	public int progressOut = 0;

	public ArrayList<BlueprintId> currentPage;

	public int selected = -1;

	public EntityPlayer uploadingPlayer = null;
	public EntityPlayer downloadingPlayer = null;

	public int pageId = 0;

	public TileBlueprintLibrary() {

	}

	@Override
	public void initialize() {
		super.initialize();

		if (worldObj.isRemote) {
			setCurrentPage(BuildCraftBuilders.clientDB.getPage (pageId));
		}
	}

	public void setCurrentPage(ArrayList<BlueprintId> newPage) {
		currentPage = newPage;
		selected = -1;
	}

	public void pageNext () {
		if (pageId < BuildCraftBuilders.clientDB.getPageNumber() - 1) {
			pageId++;
		}

		setCurrentPage(BuildCraftBuilders.clientDB.getPage (pageId));
	}

	public void pagePrev () {
		if (pageId > 0) {
			pageId--;
		}

		setCurrentPage(BuildCraftBuilders.clientDB.getPage (pageId));
	}

	public void deleteSelectedBpt() {
		if (selected != -1) {
			BuildCraftBuilders.clientDB.deleteBlueprint(currentPage
					.get(selected));

			if (pageId > BuildCraftBuilders.clientDB.getPageNumber() - 1
					&& pageId > 0) {
				pageId--;
			}

			setCurrentPage(BuildCraftBuilders.clientDB.getPage (pageId));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		inv.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		inv.writeToNBT(nbttagcompound);
	}

	@Override
	public int getSizeInventory() {
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result = inv.decrStackSize(i, j);

		if (i == 0) {
			if (getStackInSlot(0) == null) {
				progressIn = 0;
			}
		}

		if (i == 2) {
			if (getStackInSlot(2) == null) {
				progressOut = 0;
			}
		}

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);

		if (i == 0) {
			if (getStackInSlot(0) != null && getStackInSlot(0).getItem() instanceof ItemBlueprint) {
				progressIn = 1;
			} else {
				progressIn = 0;
			}
		}

		if (i == 2) {
			if (getStackInSlot(2) != null && getStackInSlot(2).getItem() instanceof ItemBlueprint) {
				progressOut = 1;
			} else {
				progressOut = 0;
			}
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public String getInventoryName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
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
		if (progressIn == 100 && getStackInSlot(1) == null) {
			setInventorySlotContents(1, getStackInSlot(0));
			setInventorySlotContents(0, null);

			BlueprintBase bpt = ItemBlueprint.loadBlueprint(getStackInSlot(1));

			if (bpt != null && uploadingPlayer != null) {
				RPCHandler.rpcPlayer(this, "downloadBlueprintToClient",
						uploadingPlayer, bpt.id, bpt.getData());
				uploadingPlayer = null;
			}
		}

		if (progressOut == 100 && getStackInSlot(3) == null) {
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
		if (isOuputConsistent()) {
			if (selected > -1 && selected < currentPage.size()) {
				BlueprintBase bpt = BuildCraftBuilders.clientDB
						.load(currentPage.get(selected));

				RPCHandler.rpcServer(this, "uploadBlueprintToServer", bpt.id,
						bpt.getData());
			} else {
				RPCHandler.rpcServer(this, "uploadBlueprintToServer", null,
						null);
			}
		}
	}

	@RPC (RPCSide.SERVER)
	public void uploadBlueprintToServer (BlueprintId id, byte [] data) {
		try {
			if (data != null) {
				NBTTagCompound nbt = CompressedStreamTools.func_152457_a(data, NBTSizeTracker.field_152451_a);
				BlueprintBase bpt = BlueprintBase.loadBluePrint(nbt);
				bpt.setData(data);
				bpt.id = id;
				BuildCraftBuilders.serverDB.add(bpt);
				setInventorySlotContents(3, bpt.getStack());
			} else {
				setInventorySlotContents(3, getStackInSlot(2));
			}

			setInventorySlotContents(2, null);

			downloadingPlayer = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RPC (RPCSide.CLIENT)
	public void downloadBlueprintToClient (BlueprintId id, byte [] data) {
		try {
			NBTTagCompound nbt = CompressedStreamTools.func_152457_a(data, NBTSizeTracker.field_152451_a);
			BlueprintBase bpt = BlueprintBase.loadBluePrint(nbt);
			bpt.setData(data);
			bpt.id = id;

			BuildCraftBuilders.clientDB.add(bpt);
			setCurrentPage(BuildCraftBuilders.clientDB.getPage(pageId));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void selectBlueprint (int index) {
		selected = index;
	}

	private boolean isOuputConsistent () {
		if (selected == -1 || getStackInSlot(2) == null) {
			return false;
		}

		return (getStackInSlot(2).getItem() instanceof ItemBlueprintStandard
				&& currentPage.get(selected).kind == Kind.Blueprint) ||
				(getStackInSlot(2).getItem() instanceof ItemBlueprintTemplate
				&& currentPage.get(selected).kind == Kind.Template);
	}
}
