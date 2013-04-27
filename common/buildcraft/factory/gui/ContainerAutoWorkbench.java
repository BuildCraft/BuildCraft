/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import buildcraft.core.gui.BuildCraftContainer;
import buildcraft.factory.TileAutoWorkbench;

public class ContainerAutoWorkbench extends BuildCraftContainer {

	TileAutoWorkbench parent = null;

	public ContainerAutoWorkbench(InventoryPlayer playerInventory, TileAutoWorkbench tileEntity) {
		super(tileEntity.getSizeInventory());
		
		this.parent = tileEntity;
		
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				this.addSlotToContainer(new Slot(this.parent, y * 3 + x, x * 18 + 30, y * 18 + 17));
			}
		}
		this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.parent, parent.getCraftResult(), 0, 124, 35)); 
		
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 9; y++) {
				this.addSlotToContainer(new Slot(playerInventory, x * 9 + y + 9, y * 18 + 8, x * 18 + 84));
			}
		}
		
		for (int x = 0; x < 9; x++){
			this.addSlotToContainer(new Slot(playerInventory, x, x * 18 + 8, 142));
		}

		this.onCraftMatrixChanged(parent);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		Slot output = (Slot) inventorySlots.get(slot);
		ItemStack itemstack = null;
		if (output != null && output.getHasStack()){
			ItemStack itemstack1 = output.getStack();
			itemstack = itemstack1.copy();
			
			if (slot == 9 ? !mergeItemStack(itemstack1, 10, 46, true) : slot < 37 ? 
					slot < 10 ? !this.mergeItemStack(itemstack1, 10, 46, false) : !this.mergeItemStack(itemstack1, 37, 46, false) : 
						!this.mergeItemStack(itemstack1, 10, 37, false)){
				itemstack = null;
			}else if (itemstack1.stackSize == 0) {
				output.putStack(null);
			} else {
				output.onSlotChanged();
			}
		}
		
		if (slot == 9){
			((SlotCrafting) output).onPickupFromSlot(player, itemstack);
		}
		this.parent.onInventoryChanged();
		return itemstack;
	}
	
	@Override
	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer) {
		if (par1 != 9){
			super.retrySlotClick(par1, par2, par3, par4EntityPlayer);
		}
	}
	
	@Override
	public void onCraftMatrixChanged(IInventory inventory) {
		inventory.onInventoryChanged();
		
		super.onCraftMatrixChanged(inventory);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.parent.isUseableByPlayer(player);
	}
}
