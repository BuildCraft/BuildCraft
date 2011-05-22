package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.Utils;

public class TileFiller extends TileEntity implements IInventory {

	Box box;

    public TileFiller()
    {
        contents = new ItemStack[36];
    }
    
    boolean init = false;
    
    long lastTick = 0;
    boolean lastPower = false;
    
	public void updateEntity () {
		if (!init) {
			init = true;

			IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
					zCoord);

			if (a != null) {
				box = new Box (a);				

				if (a instanceof TileMarker) {
					((TileMarker) a).removeFromWorld();
				}
			}			
		}

		if (box != null) {
			box.createLasers(worldObj, 2);
		}
	}
	
	public void checkPower () {
		boolean power = worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord);
		
		if (power != lastPower) {
			lastPower = power;
			
			work();
		}
	}
	
	private void work () {		
		if (worldObj.getWorldTime() - lastTick >= 10) {
			lastTick = worldObj.getWorldTime();
			
			if (box != null) {
				
				boolean found = false;
				int xSlot = 0, ySlot = 0, zSlot = 0;
				
				for (int y = box.yMin; y <= box.yMax && !found; ++y) {
					for (int x = box.xMin; x <= box.xMax && !found; ++x) {
						for (int z = box.zMin; z <= box.zMax && !found; ++z) {
							if (worldObj.getBlockId(x, y, z) == 0) {
								xSlot = x;
								ySlot = y;
								zSlot = z;
								
								found = true;
							}
						}
					}
				}
				
				if (found) {
					for (int s = 0; s < getSizeInventory(); ++s) {
						if (getStackInSlot(s) != null
								&& getStackInSlot(s).stackSize > 0
								&& getStackInSlot(s).getItem() instanceof ItemBlock) {
							
							ItemStack stack = decrStackSize(s, 1);
							stack.getItem().onItemUse(stack, null, worldObj,
									xSlot, ySlot + 1, zSlot, 0);

													
							break;
						}
					}
				} else {
					box.deleteLasers();
					box = null;
				}
			}
		}
		
	}	

    public int getSizeInventory()
    {
        return 27;
    }

    public ItemStack getStackInSlot(int i)
    {
        return contents[i];
    }

    public ItemStack decrStackSize(int i, int j)
    {
        if(contents[i] != null)
        {
            if(contents[i].stackSize <= j)
            {
                ItemStack itemstack = contents[i];
                contents[i] = null;
                onInventoryChanged();
                return itemstack;
            }
            ItemStack itemstack1 = contents[i].splitStack(j);
            if(contents[i].stackSize == 0)
            {
                contents[i] = null;
            }
            onInventoryChanged();
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
        onInventoryChanged();
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
    
    private ItemStack contents[];
    
    public void destroy () {
    	if (box != null) {
    		box.deleteLasers();    		
    	}
    	
    	Utils.dropItems(worldObj, this, xCoord, yCoord, zCoord);
    }

}
