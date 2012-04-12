/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BlockContents;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.BluePrintBuilder;
import net.minecraft.src.buildcraft.core.BluePrintBuilder.Mode;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileBuilder extends TileBuildCraft implements IInventory, IPowerReceptor {
	
	private ItemStack items [] = new ItemStack [28];
	
	private BluePrintBuilder bluePrintBuilder;
	private int currentBluePrintId = -1;
	
	public @TileNetworkData Box box = new Box ();
	
	private PowerProvider powerProvider;
	
	public TileBuilder () {
		super ();
		
		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(10, 25, 25, 25, 25);
		powerProvider.configurePowerPerdition(25, 1);
	}
	
	public void initialize () {
		super.initialize();
		
		initalizeBluePrint();
	}
	
	public void initalizeBluePrint () {
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		if (items[0] == null
				|| items[0].getItem().shiftedIndex != BuildCraftBuilders.templateItem.shiftedIndex) {
			currentBluePrintId = -1;
			bluePrintBuilder = null;

			if (box.isInitialized()) {
				box.deleteLasers();
				box.reset();
			}
			
			if (APIProxy.isServerSide()) {
				sendNetworkUpdate();
			}

			return;
		}
		
		if (items [0].getItemDamage() == currentBluePrintId) {
			return;
		}
		
		bluePrintBuilder = null;
		
		if (box.isInitialized()) {
			box.deleteLasers();
			box.reset();
		}
		
		BluePrint bpt = BuildCraftBuilders.bluePrints[items[0]
				.getItemDamage()];

		if (bpt == null) {
			if (APIProxy.isServerSide()) {
				CoreProxy.sendToPlayers(getUpdatePacket(), xCoord, yCoord, zCoord,
						50, mod_BuildCraftBuilders.instance);
			}
			
			return;
		}

		bpt = new BluePrint(bpt);

		Orientations o = Orientations.values()[worldObj.getBlockMetadata(
				xCoord, yCoord, zCoord)].reverse();

		if (o == Orientations.XPos) {
			// Do nothing
		} else if (o == Orientations.ZPos) {
			bpt.rotateLeft();
		} else if (o == Orientations.XNeg) {
			bpt.rotateLeft();
			bpt.rotateLeft();
		} else if (o == Orientations.ZNeg) {
			bpt.rotateLeft();
			bpt.rotateLeft();
			bpt.rotateLeft();
		}

		bluePrintBuilder = new BluePrintBuilder(bpt, xCoord, yCoord,
				zCoord);
		
		box.initialize(bluePrintBuilder);
		box.createLasers(worldObj, LaserKind.Stripes);
		currentBluePrintId = items[0].getItemDamage();
		
		if (APIProxy.isServerSide()) {
			sendNetworkUpdate();
		}
	}
	
	@Override
	public void doWork() {
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		if (powerProvider.useEnergy(25, 25, true) < 25) {
			return;
		}
		
		initalizeBluePrint();
		
		if (bluePrintBuilder != null && !bluePrintBuilder.done) {
			BlockContents contents = bluePrintBuilder.findNextBlock(worldObj,
					Mode.Template);
			
			if (contents == null && box.isInitialized()) {
				box.deleteLasers();
				box.reset();
				
				if (APIProxy.isServerSide()) {
					sendNetworkUpdate();
				}
				
				return;
			}
			
			if (!API.softBlock(contents.blockId)) {
				Block.blocksList[contents.blockId].dropBlockAsItem(worldObj,
						contents.x, contents.y, contents.z, worldObj
								.getBlockMetadata(contents.x, contents.y,
										contents.z), 0);
				
				worldObj.setBlockWithNotify(contents.x, contents.y, contents.z,
						0);				
			} else {
				for (int s = 1; s < getSizeInventory(); ++s) {
					if (getStackInSlot(s) != null
							&& getStackInSlot(s).stackSize > 0
							&& getStackInSlot(s).getItem() instanceof ItemBlock) {

						ItemStack stack = decrStackSize(s, 1);
						stack.getItem().onItemUse(stack,
								BuildCraftCore.getBuildCraftPlayer(worldObj),
								worldObj, contents.x, contents.y + 1,
								contents.z, 0);

						break;
					}
				}
			}
		}
		
	}

	@Override
	public int getSizeInventory() {
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items [i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result;
		if (items [i] == null) {
			result = null;
		} else if (items [i].stackSize > j) {
			result = items [i].splitStack(j);
		} else {
			ItemStack tmp = items [i];
			items [i] = null;
			result = tmp;
		}
		
		if (i == 0) {
			initalizeBluePrint();
		}
		
		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
		if (i == 0) {
			initalizeBluePrint();
		}
		
	}

	@Override
	public String getInvName() {
		return "Builder";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}
	
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        items = new ItemStack[getSizeInventory()];
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xff;
            if(j >= 0 && j < items.length)
            {
                items[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
        
        if (nbttagcompound.hasKey("box")) {
        	box.initialize(nbttagcompound.getCompoundTag("box"));
        }

    }

    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();
        for(int i = 0; i < items.length; i++)
        {
            if(items[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                items[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
        
        if (box.isInitialized()) {
        	NBTTagCompound boxStore = new NBTTagCompound();
        	((Box)box).writeToNBT(boxStore);
        	nbttagcompound.setTag("box", boxStore);
        }
    }

    @Override
    public void invalidate () {
    	destroy ();
    }
    
    @Override
	public void destroy() {		
		if (box.isInitialized()) {
			box.deleteLasers();
		}
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}
	
	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleDescriptionPacket(packet);		
		
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);			
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
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

	@Override
	public int powerRequest() {
		return powerProvider.maxEnergyReceived;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1){
		if (this.items[var1] == null) return null;
		
		ItemStack var2 = this.items[var1];
		this.items[var1] = null;
		return var2;
	}
	
}
