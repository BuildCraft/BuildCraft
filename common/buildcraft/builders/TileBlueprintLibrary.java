package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.BlueprintMeta;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BptBase;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;
import buildcraft.core.network.NetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.google.common.io.ByteStreams;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import paulscode.sound.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileBlueprintLibrary extends TileBuildCraft implements IInventory {
	public ItemStack[] stack = new ItemStack[4];

	public int progressIn = 0;
	public int progressOut = 0;

	public String owner = "";

	private ArrayList<BptBase> currentPage;

	public LinkedList <String> currentBlueprint = new LinkedList <String> ();

	int selected = -1;
	boolean locked = false;

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

	@SideOnly (Side.CLIENT)
	public void updateCurrentNames() {
		currentBlueprint.clear();
		RPCHandler.rpcServer(this, "fetchPage", 1);
	}

	@RPC (RPCSide.SERVER)
	public void fetchPage (int pageId, RPCMessageInfo info) {
		for (int i = 0; i < BuildCraftBuilders.LIBRARY_PAGE_SIZE; ++i) {
			BlueprintMeta dummyMeta = new BlueprintMeta ();
			dummyMeta.name = "Blueprint #" + i;

			RPCHandler.rpcPlayer(this, "receiveBlueprintMeta", info.sender, dummyMeta);
		}
	}

	@RPC (RPCSide.CLIENT)
	public void receiveBlueprintMeta (BlueprintMeta meta) {
		currentBlueprint.add(meta.name);
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
		// TODO Auto-generated method stub
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

		if (progressIn == 100 && stack[1] == null) {
			setInventorySlotContents(1, stack[0]);
			setInventorySlotContents(0, null);
			BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(stack[1].getItemDamage());

			if (bpt != null) {
				BptPlayerIndex index = BuildCraftBuilders.getPlayerIndex(BuildersProxy.getOwner(this));

				try {
					index.addBlueprint(bpt.file);
					setCurrentPage(true);
					setCurrentPage(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
}
