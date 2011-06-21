package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.BlockContents;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.BluePrintBuilder;
import net.minecraft.src.buildcraft.core.BluePrintBuilder.Mode;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.TileCurrentPowered;
import net.minecraft.src.buildcraft.core.Utils;

public class TileBuilder extends TileCurrentPowered implements IInventory {

	ItemStack items [] = new ItemStack [28];
	
	BluePrintBuilder bluePrintBuilder;
	int currentBluePrintId = -1;
	IBox box;
	
	public TileBuilder () {
		super ();
		latency = 10;
	}
	
	public void initialize () {
		super.initialize();
		
		initalizeBluePrint();
	}
	
	public void initalizeBluePrint () {
		if (items[0] == null
				|| items[0].getItem().shiftedIndex != BuildCraftBuilders.templateItem.shiftedIndex) {
			currentBluePrintId = -1;
			bluePrintBuilder = null;

			if (box != null) {
				box.deleteLasers();
				box = null;
			}

			return;
		}
		
		if (items [0].getItemDamage() == currentBluePrintId) {
			return;
		}
		
		bluePrintBuilder = null;
		
		if (box != null) {
			box.deleteLasers();
			box = null;
		}
		
		BluePrint bpt = BuildCraftBuilders.bluePrints[items[0]
				.getItemDamage()];

		if (bpt == null) {
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
		
		box = bluePrintBuilder.getBox();
		box.createLasers(worldObj, LaserKind.Stripes);
		currentBluePrintId = items[0].getItemDamage();
	}
	
	@Override
	protected void doWork() {		
		initalizeBluePrint();
		
		if (bluePrintBuilder != null && !bluePrintBuilder.done) {
			BlockContents contents = bluePrintBuilder.findNextBlock(worldObj,
					Mode.Template);
			
			if (contents == null) {
				box.deleteLasers();
				box = null;
				return;
			}
			
			if (contents.blockId != 0) {
				Block.blocksList[contents.blockId].dropBlockAsItem(worldObj,
						contents.x, contents.y, contents.z, worldObj
								.getBlockMetadata(contents.x, contents.y,
										contents.z));
				
				worldObj.setBlockWithNotify(contents.x, contents.y, contents.z,
						0);				
			} else {
				for (int s = 1; s < getSizeInventory(); ++s) {
					if (getStackInSlot(s) != null
							&& getStackInSlot(s).stackSize > 0
							&& getStackInSlot(s).getItem() instanceof ItemBlock) {

						ItemStack stack = decrStackSize(s, 1);
						stack.getItem().onItemUse(stack, null, worldObj,
								contents.x,  contents.y + 1, contents.z, 0);

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
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
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
                items[j] = new ItemStack(nbttagcompound1);
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
        for(int i = 0; i < items.length; i++)
        {
            if(items[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                items[i].writeToNBT(nbttagcompound1);
                nbttaglist.setTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("Items", nbttaglist);
        
        if (box != null) {
        	NBTTagCompound boxStore = new NBTTagCompound();
        	((Box)box).writeToNBT(boxStore);
        	nbttagcompound.setTag("box", boxStore);
        }
    }

	public void delete() {
		Utils.dropItems(worldObj, this, xCoord, yCoord, zCoord);		
	}

	
}
