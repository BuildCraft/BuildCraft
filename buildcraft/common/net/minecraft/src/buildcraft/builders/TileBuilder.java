package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.BlockContents;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.BluePrintBuilder;
import net.minecraft.src.buildcraft.core.TileCurrentPowered;

public class TileBuilder extends TileCurrentPowered implements IInventory {

	ItemStack items [] = new ItemStack [28];
	
	public TileBuilder () {
		super ();
		latency = 1;
	}
	
	@Override
	protected void doWork() {		
		if (items[0] != null
				&& items[0].getItem().shiftedIndex == mod_BuildCraftBuilders.templateItem.shiftedIndex) {
			
			BluePrint bpt = mod_BuildCraftBuilders.bluePrints[items[0]
					.getItemDamage()];
			
			if (bpt == null) {
				return;
			}
			
			bpt = new BluePrint(bpt);
			
			Orientations o = Orientations.values()[worldObj.getBlockMetadata(xCoord,
					yCoord, zCoord)].reverse();
			
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
						
			BluePrintBuilder builder = new BluePrintBuilder(bpt, xCoord,
					yCoord, zCoord);
			
			while (!builder.done) {
				BlockContents contents = builder.findNextBlock(worldObj,
						BluePrintBuilder.Mode.Template);
				
				if (builder.done) {
					break;
				}
								
				if (contents.blockId == 0) {
					worldObj.setBlockWithNotify(contents.x, contents.y,
							contents.z, Block.stone.blockID);
				} else {
					worldObj.setBlockWithNotify(contents.x, contents.y,
							contents.z, 0);
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
		if (items [i] == null) {
			return null;
		} else if (items [i].stackSize > j) {
			return items [i].splitStack(j);
		} else {
			ItemStack tmp = items [i];
			items [i] = null;
			return tmp;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
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

	
}
