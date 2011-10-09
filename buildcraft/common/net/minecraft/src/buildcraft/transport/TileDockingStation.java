/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
 
package net.minecraft.src.buildcraft.transport;

import java.util.List;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.IInventory;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;

public class TileDockingStation extends TileEntity implements ILiquidContainer, ISpecialInventory{
													
	public TileDockingStation () {
	}
	
@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
	}

	@Override
	public String getInvName() {
		return "DockingStation";
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
			
	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}
	
	private AxisAlignedBB getCheckBox(Orientations orientation, int distance)
	{		
		if(orientation == Orientations.Unknown)
		{
			return null;
		}
		Position p1 = new Position(xCoord, yCoord, zCoord, orientation);
		Position p2 = new Position(xCoord, yCoord, zCoord, orientation);

		switch (orientation) {
		case XPos:
			p1.x += distance;
			p2.x += 1 + distance;
			break;
		case XNeg:
			p1.x -= (distance - 1);
			p2.x -= distance;
			break;
		case YPos:
		case YNeg:
			p1.x += distance + 1;
			p2.x -= distance;
			p1.z += distance + 1;
			p2.z -= distance;
			break;
		case ZPos:
			p1.z += distance;
			p2.z += distance + 1;
			break;
		case ZNeg:
			p1.z -= (distance - 1);
			p2.z -= distance;
			break;
		}

		switch (orientation) {
		case XPos:
		case XNeg:
			p1.y += distance + 1;
			p2.y -= distance;
			p1.z += distance + 1;
			p2.z -= distance;
			break;
		case YPos:
			p1.y += distance + 1;
			p2.y += distance;
			break;
		case YNeg:
			p1.y -= (distance - 1);
			p2.y -= distance;
			break;
		case ZPos:
		case ZNeg:
			p1.y += distance + 1;
			p2.y -= distance;
			p1.x += distance + 1;
			p2.x -= distance;
			break;
		}

		Position min = p1.min(p2);
		Position max = p1.max(p2);

		return AxisAlignedBB.getBoundingBoxFromPool(min.x, min.y, min.z, max.x,
				max.y, max.z);	
	}
	
	private EntityMinecart getCart()
	{
		AxisAlignedBB box = getCheckBox(Orientations.YPos, 1);
		
		if(box == null) {
			return null;			
		}
		
		@SuppressWarnings("rawtypes")
		List list = worldObj.getEntitiesWithinAABB(
				net.minecraft.src.Entity.class, box);

		for (int g = 0; g < list.size(); g++) {
			if(list.get(g) instanceof EntityMinecart) {
					return (EntityMinecart) list.get(g);
					}
		}
		return null;
	}
	
	public ItemStack checkExtractGeneric(IInventory inventory,
			boolean doRemove, Orientations from) {
		for (int k = 0; k < inventory.getSizeInventory(); ++k) {
			if (inventory.getStackInSlot(k) != null
					&& inventory.getStackInSlot(k).stackSize > 0) {

				ItemStack slot = inventory.getStackInSlot(k);

				if (slot != null && slot.stackSize > 0) {
					if (doRemove) {
						return inventory.decrStackSize(k, 1);
					} else {
						return slot;
					}
				}
			}
		}

		return null;
	}

	//LIQUID START
	
	@Override
	public int fill (Orientations from, int quantity, int id, boolean doFill)
	{
		return 0;
	}
	
	@Override
	public int empty (int quantityMax, boolean doEmpty)
	{
		return 0;
	}
	
	@Override
	public int getLiquidQuantity ()
	{
		return 0;
	}
	
	@Override
	public int getCapacity ()
	{
		return 0;
	}
	
	@Override
	public int getLiquidId ()
	{
		return 0;
	}
	
	//LIQUID END
	
	//ITEMS START
	@Override
	public boolean addItem (ItemStack stack, boolean doAdd, Orientations from)
	{
		EntityMinecart cart = getCart();
		if(cart != null)
		{
			if (!cart.isDead && cart.minecartType == 1) {
						StackUtil utils = new StackUtil(stack);
						return utils.checkAvailableSlot((IInventory) cart, doAdd, from);
			}
		}
		return false;
	}
	
	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from)
	{
		EntityMinecart cart = getCart();
		if(cart != null)
		{
			if (!cart.isDead && cart.minecartType == 1) {
						return checkExtractGeneric((IInventory) cart, doRemove,
								from);
			}
		}
		return null;
	}
	//ITEMS END
}