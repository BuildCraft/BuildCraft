/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.Translation;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.NetworkData;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockScanner;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IBoxProvider;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.blueprints.Template;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.utils.Utils;

public class TileArchitect extends TileBuildCraft implements IInventory, IBoxProvider {

	private static final int SCANNER_ITERATION = 100;

	public int computingTime = 0;
	public String currentAuthorName = "";
	@NetworkData
	public Box box = new Box();
	@NetworkData
	public String name = "";
	@NetworkData
	public BlueprintReadConfiguration readConfiguration = new BlueprintReadConfiguration();

	private SimpleInventory inv = new SimpleInventory(2, "Architect", 1);
	private BlueprintBase writingBlueprint;
	private BptContext writingContext;
	private BlockScanner blockScanner;

	public TileArchitect() {
		box.kind = Kind.STRIPES;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!worldObj.isRemote && blockScanner != null) {
			if (blockScanner.blocksLeft() != 0) {
				for (BlockIndex index : blockScanner) {
					writingBlueprint.readFromWorld(writingContext, this,
							index.x, index.y, index.z);
				}

				computingTime = (int) ((1 - (float) blockScanner.blocksLeft()
						/ (float) blockScanner.totalBlocks()) * 100);

				if (blockScanner.blocksLeft() == 0) {
					writingBlueprint.readEntitiesFromWorld (writingContext, this);

					Translation transform = new Translation();

					transform.x = -writingContext.surroundingBox().pMin().x;
					transform.y = -writingContext.surroundingBox().pMin().y;
					transform.z = -writingContext.surroundingBox().pMin().z;

					writingBlueprint.translateToBlueprint(transform);

					ForgeDirection o = ForgeDirection.values()[worldObj.getBlockMetadata(
							xCoord, yCoord, zCoord)].getOpposite();

					writingBlueprint.rotate = readConfiguration.rotate;
					writingBlueprint.excavate = readConfiguration.excavate;

					if (writingBlueprint.rotate) {
						if (o == ForgeDirection.EAST) {
							// Do nothing
						} else if (o == ForgeDirection.SOUTH) {
							writingBlueprint.rotateLeft(writingContext);
							writingBlueprint.rotateLeft(writingContext);
							writingBlueprint.rotateLeft(writingContext);
						} else if (o == ForgeDirection.WEST) {
							writingBlueprint.rotateLeft(writingContext);
							writingBlueprint.rotateLeft(writingContext);
						} else if (o == ForgeDirection.NORTH) {
							writingBlueprint.rotateLeft(writingContext);
						}
					}
				}
			} else if (writingBlueprint.getData() != null) {
				createBlueprint();

				computingTime = 0;
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!worldObj.isRemote) {
			if (!box.isInitialized()) {
				IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord,
						yCoord, zCoord);

				if (a != null) {
					box.initialize(a);
					a.removeFromWorld();
					sendNetworkUpdate();
				}
			}
		}
	}

	public void createBlueprint() {
		writingBlueprint.id.name = name;
		BuildCraftBuilders.serverDB.add(writingBlueprint);

		setInventorySlotContents(1, writingBlueprint.getStack());
		setInventorySlotContents(0, null);

		writingBlueprint = null;
		writingContext = null;
		blockScanner = null;
	}

	@RPC (RPCSide.SERVER)
	public void handleClientSetName(String nameSet) {
		name = nameSet;
		RPCHandler.rpcBroadcastPlayers(this, "setName", name);
	}

	@RPC
	public void setName (String name) {
		this.name = name;
	}

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result = inv.decrStackSize(i, j);

		if (i == 0) {
			initializeComputing();
		}

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);

		if (i == 0) {
			initializeComputing();
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public String getInventoryName() {
		return "Template";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		// For now, scan states don't get saved. Would need to save
		// blueprints too.
		/*if (nbttagcompound.hasKey("scanner")) {
			blockScanner = new BlockScanner();
			blockScanner.readFromNBT(nbttagcompound.getCompoundTag("scanner"));
		}*/

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		inv.readFromNBT(nbttagcompound);

		name = nbttagcompound.getString("name");
		currentAuthorName = nbttagcompound.getString("lastAuthor");

		if (nbttagcompound.hasKey("readConfiguration")) {
			readConfiguration.readFromNBT(nbttagcompound.getCompoundTag("readConfiguration"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		// For now, scan states don't get saved. Would need to save
		// blueprints too.
		/*if (blockScanner != null) {
			NBTTagCompound scanner = new NBTTagCompound();
			blockScanner.writeToNBT(scanner);
			nbttagcompound.setTag("scanner", scanner);
		}*/

		if (box.isInitialized()) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}

		inv.writeToNBT(nbttagcompound);

		nbttagcompound.setString("name", name);
		nbttagcompound.setString("lastAuthor", currentAuthorName);

		NBTTagCompound readConf = new NBTTagCompound();
		readConfiguration.writeToNBT(readConf);
		nbttagcompound.setTag("readConfiguration", readConf);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	private void initializeComputing() {
		if (getWorld().isRemote) {
			return;
		}

		if (!box.isInitialized()) {
			return;
		} else if (blockScanner == null) {
			if (getStackInSlot(0) != null && getStackInSlot(0).getItem() instanceof ItemBlueprint
					&& getStackInSlot(1) == null) {
				if (!box.isInitialized() || getStackInSlot(1) != null) {
					return;
				}

				blockScanner = new BlockScanner(box, getWorld(), SCANNER_ITERATION);

				if (getStackInSlot(0).getItem() instanceof ItemBlueprintStandard) {
					writingBlueprint = new Blueprint(box.sizeX(), box.sizeY(), box.sizeZ());
				} else if (getStackInSlot(0).getItem() instanceof ItemBlueprintTemplate) {
					writingBlueprint = new Template(box.sizeX(), box.sizeY(), box.sizeZ());
				}

				writingContext = writingBlueprint.getContext(worldObj, box);
				writingContext.readConfiguration = readConfiguration;

				writingBlueprint.id.name = name;
				writingBlueprint.author = currentAuthorName;
				writingBlueprint.anchorX = xCoord - box.xMin;
				writingBlueprint.anchorY = yCoord - box.yMin;
				writingBlueprint.anchorZ = zCoord - box.zMin;
			}
		} else {
			blockScanner = null;
			writingBlueprint = null;
			writingContext = null;
		}
	}

	public int getComputingProgressScaled(int scale) {
		return (int) ((float) computingTime / (float) 100 * scale);
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public Box getBox() {
		return box;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new Box (this).extendToEncompass(box).getBoundingBox();
	}

	@RPC (RPCSide.SERVER)
	private void setReadConfiguration (BlueprintReadConfiguration conf) {
		readConfiguration = conf;
		sendNetworkUpdate();
	}

	public void rpcSetConfiguration (BlueprintReadConfiguration conf) {
		readConfiguration = conf;
		RPCHandler.rpcServer(this, "setReadConfiguration", conf);
	}
}