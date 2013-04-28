/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.TransactorRoundRobin;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;

public class TileAutoWorkbench extends TileEntity implements ISpecialInventory {
	private IInventory craftResult = new InventoryCraftResult();
	
	private InventoryCrafting crafting = new InventoryCrafting(new Container() {
		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			return false;
		}
		
		@Override
		public void onCraftMatrixChanged(IInventory inventory) {}
	}, 3, 3);
	
	@Override
	public void onInventoryChanged() {
		craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(crafting, this.worldObj));	
		super.onInventoryChanged();
	}
	
	public IInventory getCraftResult() {
		return this.craftResult;
	}

	@Override
	public int getSizeInventory() {
		return this.crafting.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.crafting.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack item = this.crafting.decrStackSize(slot, amount);
		super.onInventoryChanged();
		return item;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.crafting.setInventorySlotContents(slot, stack);
		super.onInventoryChanged();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return this.crafting.getStackInSlotOnClosing(slot);
	}
	
	@Override
	public String getInvName() {
		return "AutoCrafting";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) == this;
	}
	
	public boolean canRefillFromNeighbour(){
		ItemStack[] required = new ItemStack[this.getSizeInventory()];
		
		for (int slot = 0; slot < this.getSizeInventory(); slot++){
			ItemStack item = this.getStackInSlot(slot);
			
			if (item != null && item.stackSize == 1 && !item.getItem().hasContainerItem()){
				required[slot] = item.copy();
			}
		}
		
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS){
			TileEntity tileEntity = this.worldObj.getBlockTileEntity(side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord);
			
			if (tileEntity instanceof IInventory){
				IInventory inventory = (IInventory) tileEntity;
				
				ItemStack remaining = null;
				for (int slot = 0; slot < inventory.getSizeInventory(); slot++){
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (item != null){
						remaining = item.copy();
						
						for (int index = 0; index < required.length; index++){
							if (required[index] != null && item.isItemEqual(required[index])){
								required[index] = null;
								
								if (remaining.stackSize == 1){
									remaining = null;
									break;
								}else{
									remaining.stackSize--;
								}
							}
						}
					}
				}
			}
		}
		
		for (int index = 0; index < required.length; index++){
			if (required[index] != null) return false;
		}
		
		return true;
	}
	
	public void refillFromNeighbour(){
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS){
			TileEntity tileEntity = this.worldObj.getBlockTileEntity(side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord);
			
			/*
			 * perhaps have this work with ISpecialInvenotry?
			 */
			
			if (tileEntity instanceof IInventory){
				IInventory inventory = (IInventory) tileEntity;
				
				for (int slot = 0; slot < inventory.getSizeInventory(); slot++){
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (item != null){
						for (int slot1 = 0; slot1 < this.getSizeInventory(); slot1++){
							ItemStack item1 = this.getStackInSlot(slot1);
							if (item1 != null && item1.stackSize == 1 && item.isItemEqual(item1)){
								item1.stackSize++;
								inventory.decrStackSize(slot, 1);
							}
							
							if (this.canCraft()) return;
						}
					}
				}
			}
		}
	}
	
	public void craft(){
        for (int slot = 0; slot < this.getSizeInventory(); slot++){
        	ItemStack item = this.getStackInSlot(slot);
        	
        	if (item != null){
        		if (item.getItem().hasContainerItem()){
                    ItemStack container = item.getItem().getContainerItemStack(item);
                    
                    if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()){
						this.worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, "random.break", 0.8F, 0.8F + this.worldObj.rand.nextFloat() * 0.4F);
						
                        container = null;
                    }
		            
                    this.crafting.setInventorySlotContents(slot, container);
                }else{
                	this.decrStackSize(slot, 1);                	
                }
        	}
        }
    }
	
	public boolean canCraft() {
		if (this.craftResult.getStackInSlot(0) == null){
			return false;
		}
		
		for (int slot = 0; slot < this.getSizeInventory(); slot++){
			ItemStack stack = this.getStackInSlot(slot);
			
			if (stack != null && stack.stackSize <= 1 && !stack.getItem().hasContainerItem()){
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		Utils.readStacksFromNBT(nbt, "stackList", this);
		this.onInventoryChanged();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		Utils.writeStacksToNBT(nbt, "stackList", this);
	}

	public void openChest() {}
	public void closeChest() {}

	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		return new TransactorRoundRobin(this).add(stack, from, doAdd).stackSize;
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from, int maxItemCount) {
		ItemStack item = null;
		ItemStack result = this.craftResult.getStackInSlot(0);
		
		if (result != null){
			if (!this.canCraft()){
				if (this.canRefillFromNeighbour()){
					if (doRemove){
						this.refillFromNeighbour();						
					}
				}else{
					return null;
				}
			}
			
			if (doRemove){ 
				this.craft();
			}
			item = result.copy();
		}else{
			return null;
		}
		
		return new ItemStack[] { item };
	}

    @Override
    public boolean isInvNameLocalized(){
        return false;
    }

    @Override
    public boolean isStackValidForSlot(int slot, ItemStack stack){
        return false;
    }
}
