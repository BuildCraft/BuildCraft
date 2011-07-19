package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.FillerRegistry;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.FillerPattern;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.IPowerReceptor;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.PowerProvider;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;

public class TileFiller extends TileBuildCraft implements ISpecialInventory,
		ISynchronizedTile, IPowerReceptor {

	private Box box;
	FillerPattern currentPattern;
	boolean done = true;
	boolean forceDone = false;
    private ItemStack contents[];
    PowerProvider powerProvider;

    public TileFiller() {
        contents = new ItemStack[getSizeInventory()];
        powerProvider = BuildCraftCore.powerFramework.createPowerProvider();
        powerProvider.configure(10, 25, 25, 25, 1000);
    }
    
    public void initialize () {
    	if (!APIProxy.isClient(worldObj)) {
    		IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
    				zCoord);

    		if (a != null) {
    			box = (Box) a.getBox();				

    			if (a instanceof TileMarker) {
    				((TileMarker) a).removeFromWorld();
    			}
    		}
    	} else {
    		Utils.handleBufferedDescription(this);
    	}
		
		computeRecipe ();
    }
    
	public void updateEntity () {		
		super.updateEntity();
		
		if (box != null) {
			box.createLasers(worldObj, LaserKind.Stripes);
		} else {
			done = true;
		}
	}

	
	@Override
	public void doWork () {
		if (powerProvider.useEnergy(25, 25) < 25) {
			return;
		}
		
		if (box != null && currentPattern != null && !done) {
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
			}
		}
	}	

    public int getSizeInventory()
    {
        return 36;
    }

    public ItemStack getStackInSlot(int i)
    {
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
    	
		if (APIProxy.isServerSide()) {
			CoreProxy.sendToPlayers(getUpdatePacket(), xCoord, yCoord, zCoord,
					50, mod_BuildCraftBuilders.instance);
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
                contents[j] = new ItemStack(nbttagcompound1);
            }
        }
        
        if (nbttagcompound.hasKey("box")) {
        	box = new Box (nbttagcompound.getCompoundTag("box"));
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
                nbttaglist.setTag(nbttagcompound1);
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

    public int getInventoryStackLimit()
    {
        return 64;
    }

    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
        {
            return false;
        }
        return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
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
	public void handleDescriptionPacket(Packet230ModLoader packet) {
		if (packet.packetType != PacketIds.FillerDescription.ordinal()) {
			return;
		}			
		
		done = packet.dataInt [3] == 1;
		currentPattern = FillerRegistry.getPattern(packet.dataInt[4]);
		
		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		
		if (packet.dataInt [5] != Integer.MAX_VALUE) {	
			box = new Box(packet.dataInt, 5);
			box.createLasers(worldObj, LaserKind.Stripes);
		}		
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		if (packet.packetType != PacketIds.FillerUpdate.ordinal()) {
			return;
		}	
		
		done = packet.dataInt [3] == 1;
		currentPattern = FillerRegistry.getPattern(packet.dataInt[4]);
		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
	}
	
	public Packet getDescriptionPacket () {
		Packet230ModLoader packet = new Packet230ModLoader();
		
		packet.modId = mod_BuildCraftBuilders.instance.getId();
		packet.packetType = PacketIds.FillerDescription.ordinal();
		packet.isChunkDataPacket = true;
		
		packet.dataInt = new int [5 + Box.packetSize()];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		
		packet.dataInt [3] = (done ? 1 : 0);
		packet.dataInt[4] = (currentPattern == null ? -1 : FillerRegistry
				.getPatternNumber(currentPattern));
		
		if (box == null) {
			packet.dataInt [5] = Integer.MAX_VALUE;
		} else {
			box.setData(packet.dataInt, 5);
		}
		
		return packet;
	}
	
	public Packet230ModLoader getUpdatePacket () {
		Packet230ModLoader packet = new Packet230ModLoader();
		
		packet.modId = mod_BuildCraftBuilders.instance.getId();
		packet.packetType = PacketIds.FillerUpdate.ordinal();
		packet.isChunkDataPacket = true;
		
		packet.dataInt = new int [5];
		
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		
		packet.dataInt [3] = (done ? 1 : 0);
		packet.dataInt[4] = (currentPattern == null ? -1 : FillerRegistry
				.getPatternNumber(currentPattern));

		return packet;
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;		
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}

}
