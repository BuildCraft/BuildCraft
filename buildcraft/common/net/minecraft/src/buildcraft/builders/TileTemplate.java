package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.Utils;

public class TileTemplate extends TileEntity implements IInventory {

	private Box box;
	
	boolean initialized = false;
	
	public void updateEntity () {
		if (!initialized) {
			initialize ();
			
			initialized = true;
		}
		
		if (box != null) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}	
	
    public void initialize () {
    	IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
				zCoord);

		if (a != null) {
			box = (Box) a.getBox();			
			a.removeFromWorld();
		}			
    }
    
    public BluePrint createBluePrint () {
    	if (box == null) {
    		return null;
    	}
    	
    	int mask1 = 1;
    	int mask0 = 0;
    	
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord,
				yCoord, zCoord)) {
			mask1 = 0;
			mask0 = 1;
		}
    	
    	BluePrint result = new BluePrint(box.sizeX(), box.sizeY(), box.sizeZ());
    	
    	for (int x = box.xMin; x <= box.xMax; ++x) {
    		for (int y = box.yMin; y <= box.yMax; ++y) {
    			for (int z = box.zMin; z <= box.zMax; ++z) {
    				if (worldObj.getBlockId(x, y, z) != 0) {    					
						result.setBlockId(x - box.xMin, y - box.yMin, z
								- box.zMin, mask1);
    				} else {
    					result.setBlockId(x - box.xMin, y - box.yMin, z
								- box.zMin, mask0);
    				}
    			}
    		}
    	}
    	
    	result.anchorX = xCoord - box.xMin;
    	result.anchorY = yCoord - box.yMin;
    	result.anchorZ = zCoord - box.zMin;
    	 
		Orientations o = Orientations.values()[worldObj.getBlockMetadata(xCoord,
				yCoord, zCoord)].reverse();
		
		if (o == Orientations.XPos) {
			// Do nothing
		} else if (o == Orientations.ZPos) {
			result.rotateLeft();
			result.rotateLeft();
			result.rotateLeft();
		} else if (o == Orientations.XNeg) {
			result.rotateLeft();
			result.rotateLeft();
		} else if (o == Orientations.ZNeg) {
			result.rotateLeft();
		}
    	
		return result;
    	
    }

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getBluePrintNumber() {
		BluePrint bpt = createBluePrint();
		
		if (bpt != null) {
			return mod_BuildCraftBuilders.storeBluePrint(bpt);
		} else {
			return -1;
		}
				
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("box")) {
			box = new Box(nbttagcompound.getCompoundTag("box"));
		}

	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (box != null) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}
	}
	
    public void destroy () {
    	if (box != null) {
    		box.deleteLasers();    		
    	}
    	
    	Utils.dropItems(worldObj, this, xCoord, yCoord, zCoord);
    }

}
