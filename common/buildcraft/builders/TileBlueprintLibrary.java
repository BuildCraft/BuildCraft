package buildcraft.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.BuildersProxy;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BptBase;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.utils.Utils;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class TileBlueprintLibrary extends TileBuildCraft implements IInventory {

	public ItemStack[] stack = new ItemStack[4];

	public int progressIn = 0;
	public int progressOut = 0;

	public String owner = "";

	private ArrayList<BptBase> currentPage;
	public @TileNetworkData(staticSize=BuildCraftBuilders.LIBRARY_PAGE_SIZE) String[] currentNames = new String[BuildCraftBuilders.LIBRARY_PAGE_SIZE];
	public @TileNetworkData int selected = -1;

	public @TileNetworkData boolean locked = false;

	@Override
	public void initialize() {
		super.initialize();
		setCurrentPage(getNextPage(null));
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
	
	public void updateCurrentNames(){
		currentNames = new String[BuildCraftBuilders.LIBRARY_PAGE_SIZE];
		for(int i = 0; i < currentNames.length; i++){
			currentNames[i] = currentPage.get(i).getName();
		}
		sendNetworkUpdate();
	}
	
	public ArrayList<BptBase> getCurrentPage(){
		return currentPage;
	}
	
	public void setCurrentPage(ArrayList<BptBase> newPage){
		currentPage = newPage;
		updateCurrentNames();
	}
	
	public void deleteSelectedBpt(EntityPlayer player){
		BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(player.username);
		if (selected > -1) {
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

		Utils.readStacksFromNBT(nbttagcompound, "stack", stack);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setString("owner", owner);
		nbttagcompound.setBoolean("locked", locked);

		Utils.writeStacksToNBT(nbttagcompound, "stack", stack);
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
			if (stack[0] != null && stack[0].getItem() instanceof ItemBptBase) {
				progressIn = 1;
			} else {
				progressIn = 0;
			}
		}

		if (i == 2) {
			if (stack[2] != null && stack[2].getItem() instanceof ItemBptBase) {
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public void updateEntity() {
		if (progressIn > 0 && progressIn < 100) {
			progressIn++;
		}

		if (progressOut > 0 && progressOut < 100) {
			progressOut++;
		}

		if (progressIn == 100 && stack[1] == null) {
			setInventorySlotContents(1, stack[0]);
			setInventorySlotContents(0, null);
			BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(stack[1].getItemDamage());

			if (bpt != null) {
				BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));

				try {
					index.addBlueprint(bpt.file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (progressOut == 100 && stack[3] == null) {
			if (selected > -1) {
				setInventorySlotContents(3, new ItemStack(stack[2].itemID, 1, currentPage.get(selected).position));
			} else {
				setInventorySlotContents(3, new ItemStack(stack[2].itemID, 1, 0));
			}
			setInventorySlotContents(2, null);
		}
	}
}
