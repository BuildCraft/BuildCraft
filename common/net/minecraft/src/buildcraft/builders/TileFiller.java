/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.FillerPattern;
import net.minecraft.src.buildcraft.core.FillerRegistry;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileFiller extends TileBuildCraft implements ISpecialInventory, IPowerReceptor, IMachine {
	
	public @TileNetworkData Box box = new Box ();
	public @TileNetworkData int currentPatternId = 0;
	public @TileNetworkData	boolean done = true;
	
	FillerPattern currentPattern;
	
	boolean forceDone = false;
    private ItemStack contents[];
    PowerProvider powerProvider;

    public TileFiller() {
        contents = new ItemStack[getSizeInventory()];
        powerProvider = PowerFramework.currentFramework.createPowerProvider();
        powerProvider.configure(10, 25, 100, 25, 100);
        powerProvider.configurePowerPerdition(25, 40);
    }
    
    public void initialize () {
    	super.initialize();
    	
    	if (!APIProxy.isClient(worldObj)) {
    		IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
    				zCoord);

    		if (a != null) {
    			box.initialize(a);				

    			if (a instanceof TileMarker) {
    				((TileMarker) a).removeFromWorld();
    			}
    			
    			sendNetworkUpdate();
    		}
    	}
    	
		computeRecipe ();
    }
    
	public void updateEntity () {		
		super.updateEntity();
		
		if (box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		} else {
			done = true;
		}
		
		if (powerProvider.energyStored > 25) {
			doWork();
		}
	}
	
	@Override
	public void doWork () {
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		if (powerProvider.useEnergy(25, 25, true) < 25) {
			return;
		}
		
		if (box.isInitialized() && currentPattern != null && !done) {
			ItemStack stack = null;
			int stackId = 0;
			
			for (int s = 9; s < getSizeInventory(); ++s) {
				if (getStackInSlot(s) != null
						&& getStackInSlot(s).stackSize > 0
						&& getStackInSlot(s).getItem() instanceof ItemBlock) {

					stack = contents [s];
					stackId = s;

					break;
				}
			}
			
			done = currentPattern.iteratePattern(this, box, stack);
			
			if (stack != null && stack.stackSize == 0) {
				contents [stackId] = null;
			}
			
			if (done) {
				worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
				sendNetworkUpdate();
			}			
		}
		
		if (powerProvider.energyStored > 25) {
			doWork();
		}
	}	

    public int getSizeInventory() {
        return 36;
    }

    public ItemStack getStackInSlot(int i) {
        return contents[i];
    }

    public void computeRecipe () {
    	if (APIProxy.isClient(worldObj)) {
    		return;
    	}
    	
    	FillerPattern newPattern = FillerRegistry.findMatchingRecipe(this);
    	
    	if (newPattern == currentPattern) {
    		return;
    	}
    	
    	currentPattern = newPattern;
    	
    	if (currentPattern == null || forceDone) {
    		done = true;
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
    		currentPatternId = currentPattern.id;
    	}
    	
		if (APIProxy.isServerSide()) {
			sendNetworkUpdate ();
		}
    }
    
    public ItemStack decrStackSize(int i, int j)
    {
        if(contents[i] != null) {
            if(contents[i].stackSize <= j) {
                ItemStack itemstack = contents[i];
                contents[i] = null;
//                onInventoryChanged();
                
                computeRecipe ();
                                
                return itemstack;
            }
            
            ItemStack itemstack1 = contents[i].splitStack(j);
            
            if(contents[i].stackSize == 0) {
                contents[i] = null;
            }
//            onInventoryChanged();
            
            computeRecipe ();
            
            return itemstack1;
        } else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        contents[i] = itemstack;
        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
        
        computeRecipe ();     
//        onInventoryChanged();
    }

    public String getInvName()
    {
        return "Filler";
    }

    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        contents = new ItemStack[getSizeInventory()];
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xff;
            if(j >= 0 && j < contents.length)
            {
                contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
        
        if (nbttagcompound.hasKey("box")) {
        	box.initialize(nbttagcompound.getCompoundTag("box"));
        }        
        
        done = nbttagcompound.getBoolean("done");
        
        forceDone = done;
    }

    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();
        for(int i = 0; i < contents.length; i++)
        {
            if(contents[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                contents[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
        
        if (box != null) {
        	NBTTagCompound boxStore = new NBTTagCompound();
        	box.writeToNBT(boxStore);
        	nbttagcompound.setTag("box", boxStore);
        }
        
        nbttagcompound.setBoolean("done", done);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
            return false;
        }
        return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
    }
    
    @Override
    public void invalidate () {
    	destroy ();
    }
    
    @Override
    public void destroy () {
    	if (box != null) {
    		box.deleteLasers();
    	}
    }

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		StackUtil stackUtil = new StackUtil(stack);
		
		boolean added = false;
		
		for (int i = 9; i < contents.length;++i) {
			if (stackUtil.tryAdding(this, i, doAdd, false)) {
				added = true;
				break;
			}
		}
		
		if (added) {
			if (!doAdd) {
				return true;
			} else if (stack.stackSize == 0) {
				return true;
			} else {
				addItem(stack, added, from);
				
				return true;
			}
		}
		
		if (!added) {
			for (int i = 9; i < contents.length;++i) {
				if (stackUtil.tryAdding(this, i, doAdd, true)) {
					added = true;
					break;
				}
			}
		}
		
		if (added) {
			if (!doAdd) {
				return true;
			} else if (stack.stackSize == 0) {
				return true;
			} else {
				addItem(stack, added, from);
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		for (int i = 9; i < contents.length; ++i) {
			if (contents [i] != null) {
				if (doRemove) {
					return decrStackSize(i, 1);
				} else {
					return contents [i];
				}
			}
		}
		
		return null;
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleDescriptionPacket(packet);		
		
		currentPattern = FillerRegistry.getPattern(currentPatternId);		
		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);			
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		boolean initialized = box.isInitialized();
		
		super.handleUpdatePacket(packet);
		
		currentPattern = FillerRegistry.getPattern(currentPatternId);
		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
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
	public boolean isActive() {
		return true;
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
		return powerProvider.maxEnergyReceived;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1){
		if (this.contents[var1] == null) return null;
		ItemStack stack = this.contents[var1];
		this.contents[var1] = null;
		return stack;
	}


}
