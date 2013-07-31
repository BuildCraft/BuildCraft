/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.core.Box;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BptBase;
import buildcraft.core.blueprints.BptBlueprint;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.blueprints.BptTemplate;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import java.io.IOException;

public class TileArchitect extends TileBuildCraft implements IInventory {

	public @TileNetworkData
	Box box = new Box();
	private ItemStack items[] = new ItemStack[2];
	private boolean isComputing = false;
	public int computingTime = 0;
	public @TileNetworkData
	String name = "";
	// Use that field to avoid creating several times the same template if
	// they're the same!
	private int lastBptId = 0;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (CoreProxy.proxy.isSimulating(worldObj) && isComputing) {
			if (computingTime < 200) {
				computingTime++;
			} else {
				createBpt();
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!box.isInitialized()) {
			IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord, zCoord);

			if (a != null) {
				box.initialize(a);
				a.removeFromWorld();

			}
		}

		if (!CoreProxy.proxy.isRenderWorld(worldObj) && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}

		sendNetworkUpdate();
	}

	public void createBpt() {
		if (!box.isInitialized() || items[1] != null)
			return;

		BptBase result;
		BptContext context = null;

		if (items[0].getItem() instanceof ItemBptTemplate) {
			result = createBptTemplate();
			context = new BptContext(worldObj, null, box);
		} else {
			result = createBptBlueprint();
			context = new BptContext(worldObj, (BptBlueprint) result, box);
		}

		if (!name.equals("")) {
			result.setName(name);
		}

		result.anchorX = xCoord - box.xMin;
		result.anchorY = yCoord - box.yMin;
		result.anchorZ = zCoord - box.zMin;

		ForgeDirection o = ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite();

		if (o == ForgeDirection.EAST) {
			// Do nothing
		} else if (o == ForgeDirection.SOUTH) {
			result.rotateLeft(context);
			result.rotateLeft(context);
			result.rotateLeft(context);
		} else if (o == ForgeDirection.WEST) {
			result.rotateLeft(context);
			result.rotateLeft(context);
		} else if (o == ForgeDirection.NORTH) {
			result.rotateLeft(context);
		}

		ItemStack stack;
		if (result.equals(BuildCraftBuilders.getBptRootIndex().getBluePrint(lastBptId))) {
			result = BuildCraftBuilders.getBptRootIndex().getBluePrint(lastBptId);
			stack = BuildCraftBuilders.getBptItemStack(items[0].itemID, lastBptId, result.getName());
		} else {
			int bptId = BuildCraftBuilders.getBptRootIndex().storeBluePrint(result);
			stack = BuildCraftBuilders.getBptItemStack(items[0].itemID, bptId, result.getName());
			lastBptId = bptId;
		}

		setInventorySlotContents(1, stack);
		setInventorySlotContents(0, null);
	}

	public BptBase createBptTemplate() {
		int mask1 = 1;
		int mask0 = 0;

		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			mask1 = 0;
			mask0 = 1;
		}

		BptBase result = new BptTemplate(box.sizeX(), box.sizeY(), box.sizeZ());

		for (int x = box.xMin; x <= box.xMax; ++x) {
			for (int y = box.yMin; y <= box.yMax; ++y) {
				for (int z = box.zMin; z <= box.zMax; ++z) {
					if (!worldObj.isAirBlock(x, y, z)) {
						result.setBlockId(x - box.xMin, y - box.yMin, z - box.zMin, mask1);
					} else {
						result.setBlockId(x - box.xMin, y - box.yMin, z - box.zMin, mask0);
					}
				}
			}
		}

		return result;
	}

	private BptBase createBptBlueprint() {
		BptBlueprint result = new BptBlueprint(box.sizeX(), box.sizeY(), box.sizeZ());

		BptContext context = new BptContext(worldObj, result, box);

		for (int x = box.xMin; x <= box.xMax; ++x) {
			for (int y = box.yMin; y <= box.yMax; ++y) {
				for (int z = box.zMin; z <= box.zMax; ++z) {
					result.readFromWorld(context, this, x, y, z);
				}
			}
		}

		return result;
	}

	public void handleClientInput(char c) {
		if (c == 8) {
			if (name.length() > 0) {
				name = name.substring(0, name.length() - 1);
			}
		} else if (Character.isLetterOrDigit(c) || c == ' ') {
			if (name.length() < BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE) {
				name += c;
			}
		}
		sendNetworkUpdate();
	}

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result;
		if (items[i] == null) {
			result = null;
		} else if (items[i].stackSize > j) {
			result = items[i].splitStack(j);
		} else {
			ItemStack tmp = items[i];
			items[i] = null;
			result = tmp;
		}

		initializeComputing();

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items[i] = itemstack;

		initializeComputing();

	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (items[slot] == null)
			return null;
		ItemStack toReturn = items[slot];
		items[slot] = null;
		return toReturn;
	}

	@Override
	public String getInvName() {
		return "Template";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
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
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		lastBptId = nbttagcompound.getInteger("lastTemplateId");
		computingTime = nbttagcompound.getInteger("computingTime");
		isComputing = nbttagcompound.getBoolean("isComputing");

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
		items = new ItemStack[getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 0xff;
			if (j >= 0 && j < items.length) {
				items[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		name = nbttagcompound.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("lastTemplateId", lastBptId);
		nbttagcompound.setInteger("computingTime", computingTime);
		nbttagcompound.setBoolean("isComputing", isComputing);

		if (box.isInitialized()) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				items[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbttaglist);
		nbttagcompound.setString("name", name);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		if (box.isInitialized()) {
			box.deleteLasers();
		}
	}

	private void initializeComputing() {
		if (!box.isInitialized())
			return;
		else if (!isComputing) {
			if (items[0] != null && items[0].getItem() instanceof ItemBptBase && items[1] == null) {
				isComputing = true;
				computingTime = 0;
			} else {
				isComputing = false;
				computingTime = 0;
			}
		} else {
			if (items[0] == null || !(items[0].getItem() instanceof ItemBptBase)) {
				isComputing = false;
				computingTime = 0;
			}
		}
	}

	public int getComputingProgressScaled(int i) {
		return (computingTime * i) / 200;
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		boolean initialized = box.isInitialized();

		super.handleDescriptionPacket(packet);

		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		boolean initialized = box.isInitialized();

		super.handleUpdatePacket(packet);

		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}
}
