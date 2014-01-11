package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.Blueprint;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.blueprints.BlueprintMeta;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BptBase;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.proxy.CoreProxy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * In this implementation, the blueprint library is the interface to the
 * *local* player blueprint. The player will be able to load blueprint on his
 * environment, and save blueprints to the server environment.
 */
public class TileBlueprintLibrary extends TileBuildCraft implements IInventory {
	public ItemStack[] stack = new ItemStack[4];

	public int progressIn = 0;
	public int progressOut = 0;

	@NetworkData
	public String owner = "";

	private ArrayList<BptBase> currentPage;

	public LinkedList <String> currentBlueprint = new LinkedList <String> ();

	int selected = -1;
	boolean locked = false;

	public EntityPlayer uploadingPlayer = null;

	public TileBlueprintLibrary() {

	}

	@Override
	public void initialize() {
		super.initialize();
	}

	public ArrayList<BptBase> getNextPage(String after) {
		ArrayList<BptBase> result = new ArrayList<BptBase>();

		BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));

		String it = after;

		while (result.size() < BuildCraftBuilders.LIBRARY_PAGE_SIZE) {
			it = index.nextBpt(it);

			if (it == null) {
				break;
			}

			BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(it);

			if (bpt != null) {
				result.add(bpt);
			}
		}

		return result;
	}

	public ArrayList<BptBase> getPrevPage(String before) {
		ArrayList<BptBase> result = new ArrayList<BptBase>();

		BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));

		String it = before;

		while (result.size() < BuildCraftBuilders.LIBRARY_PAGE_SIZE) {
			it = index.prevBpt(it);

			if (it == null) {
				break;
			}

			BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(it);

			if (bpt != null) {
				result.add(bpt);
			}
		}

		return result;
	}

	public void updateCurrentNames() {
		currentBlueprint.clear();
		List <BlueprintMeta> metas = BlueprintDatabase.getPage(0, 12);

		for (BlueprintMeta meta : metas) {
			currentBlueprint.add(meta.name);
		}
	}

	public ArrayList<BptBase> getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(ArrayList<BptBase> newPage) {
		currentPage = newPage;
		selected = -1;
		updateCurrentNames();
	}

	public void setCurrentPage(boolean nextPage) {
		int index = 0;
		if (nextPage) {
			index = currentPage.size() - 1;
		}
		if (currentPage.size() > 0) {
			setCurrentPage(getNextPage(currentPage.get(index).file.getName()));
		} else {
			setCurrentPage(getNextPage(null));
		}
	}

	public void deleteSelectedBpt() {
		BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));
		if (selected > -1 && selected < currentPage.size()) {
			index.deleteBluePrint(currentPage.get(selected).file.getName());
			if (currentPage.size() > 0) {
				currentPage = getNextPage(index.prevBpt(currentPage.get(0).file.getName()));
			} else {
				currentPage = getNextPage(null);
			}
			selected = -1;
			updateCurrentNames();
		}
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
		if (stack[i] == null)
			return null;

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
		if (stack[slot] == null)
			return null;
		ItemStack toReturn = stack[slot];
		stack[slot] = null;
		return toReturn;
	}

	@Override
	public String getInvName() {
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
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		if (progressIn > 0 && progressIn < 100) {
			progressIn++;
		}

		if (progressOut > 0 && progressOut < 100) {
			progressOut++;
		}

		// On progress IN, we'll download the blueprint from the server to the
		// client, and then store it to the client.
		if (progressIn == 100 && stack[1] == null) {
			setInventorySlotContents(1, stack[0]);
			setInventorySlotContents(0, null);

			Blueprint bpt = ItemBlueprint.getBlueprint(stack [1]);

			if (bpt != null && uploadingPlayer != null) {
				RPCHandler.rpcPlayer(this, "receiveBlueprint", uploadingPlayer, bpt);

				//BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));

				/*try {
					//index.addBlueprint(bpt.file);
					setCurrentPage(true);
					setCurrentPage(false);
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
		}

		if (progressOut == 100 && stack[3] == null) {
			if (selected > -1 && selected < currentPage.size()) {
				BptBase bpt = currentPage.get(selected);
				setInventorySlotContents(3, BuildCraftBuilders.getBptItemStack(stack[2].itemID, bpt.position, bpt.getName()));
			} else {
				setInventorySlotContents(3, BuildCraftBuilders.getBptItemStack(stack[2].itemID, 0, null));
			}
			setInventorySlotContents(2, null);
		}
	}

	@RPC (RPCSide.CLIENT)
	public void receiveBlueprint (Blueprint bpt) {
		BlueprintDatabase.add(bpt);
	}
}
