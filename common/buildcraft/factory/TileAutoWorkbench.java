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
import cpw.mods.fml.common.registry.GameRegistry;

public class TileAutoWorkbench extends TileEntity implements ISpecialInventory {
	private IInventory craftResult = new InventoryCraftResult();
	
	private InventoryCrafting crafting = new InventoryCrafting(new Container() {
		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			return false;
		}
		
		@Override
		public void onCraftMatrixChanged(IInventory inventory) {
			onInventoryChanged();
		}
	}, 3, 3);
	
	@Override
	public void onInventoryChanged() {
		this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(crafting, this.worldObj));	
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
		return this.crafting.decrStackSize(slot, amount);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.crafting.setInventorySlotContents(slot, stack);
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
		return this.crafting.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) == this;
	}
	
	public boolean canRefillFromNeighbour(ForgeDirection ignore){
		ItemStack[] required = new ItemStack[this.getSizeInventory()];
		
		for (int slot = 0; slot < this.getSizeInventory(); slot++){
			ItemStack item = this.getStackInSlot(slot);
			
			if (item != null && item.stackSize == 1 && !item.getItem().hasContainerItem()){
				required[slot] = item.copy();
			}
		}
		
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS){
			
			//prevents refill function from requesting form the TileEntity that's requesting the item from this table.
			//Prevents possible StackOverflowException. Accepts null
			if (ignore == side){
				continue;
			}
			
			TileEntity tileEntity = this.worldObj.getBlockTileEntity(side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord);
			
			if (tileEntity instanceof ISpecialInventory){
				//maybe allow dazy chaining?
			}else if (tileEntity instanceof net.minecraft.inventory.ISidedInventory){
				//valilla's implementation of ISidedInvenory
				
				net.minecraft.inventory.ISidedInventory inventory = (net.minecraft.inventory.ISidedInventory) tileEntity;
				int[] slots = inventory.getSizeInventorySide(side.getOpposite().ordinal());
				
				if (slots == null || slots.length == 0){
					continue;
				}
				
				for (int slot : slots){
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (!inventory.func_102008_b(slot, item, side.getOpposite().ordinal())){
						continue;
					}
					
					if (item != null && item.stackSize > 0){
						int remaining = item.stackSize;
						
						for (int index = 0; index < required.length; index++){
							if (required[index] != null && item.isItemEqual(required[index])){
								required[index] = null;
								
								if (remaining <= 1){
									break;
								}else{
									remaining--;
								}
							}
						}
					}
				}
			}else if (tileEntity instanceof net.minecraftforge.common.ISidedInventory){
				//Forge's implementation of ISidedInventory
				
				net.minecraftforge.common.ISidedInventory inventory = (net.minecraftforge.common.ISidedInventory) tileEntity;
				int startSlot = inventory.getStartInventorySide(side.getOpposite());
				int size = inventory.getSizeInventorySide(side.getOpposite());
				
				for (int slot = startSlot; slot < startSlot + size; slot++){
					if (slot < 0){
						continue;
					}
					
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (item != null && item.stackSize > 0){
						int remaining = item.stackSize;
						
						for (int index = 0; index < required.length; index++){
							if (required[index] != null && item.isItemEqual(required[index])){
								required[index] = null;
								
								if (remaining <= 1){
									break;
								}else{
									remaining--;
								}
							}
						}
					}
				}
			}else if (tileEntity instanceof IInventory){
				IInventory inventory = Utils.getInventory((IInventory) tileEntity);
				
				for (int slot = 0; slot < inventory.getSizeInventory(); slot++){
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (item != null && item.stackSize > 0){
						int remaining = item.stackSize;
						
						for (int index = 0; index < required.length; index++){
							if (required[index] != null && item.isItemEqual(required[index])){
								required[index] = null;
								
								if (remaining <= 1){
									break;
								}else{
									remaining--;
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
	
	public void refillFromNeighbour(ForgeDirection ignore){
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS){
			//prevents refill function from requesting form the TileEntity that's requesting the item from this table.
			//Prevents possible StackOverflowException. Accepts null
			if (ignore == side){
				continue;
			}
			
			TileEntity tileEntity = this.worldObj.getBlockTileEntity(side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord);
			
			if (tileEntity instanceof ISpecialInventory){
				//maybe allow dazy chaining?
			}else if (tileEntity instanceof net.minecraft.inventory.ISidedInventory){
				//valilla's implementation of ISidedInvenory
				
				net.minecraft.inventory.ISidedInventory inventory = (net.minecraft.inventory.ISidedInventory) tileEntity;
				int[] slots = inventory.getSizeInventorySide(side.getOpposite().ordinal());
				
				if (slots == null || slots.length == 0){
					continue;
				}
				
				for (int slot : slots){
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (!inventory.func_102008_b(slot, item, side.getOpposite().ordinal())){
						continue;
					}
					
					if (item != null && item.stackSize > 0){
						int remaining = item.stackSize;
						for (int slot1 = 0; slot1 < this.getSizeInventory(); slot1++){
							ItemStack item1 = this.getStackInSlot(slot1);
							if (item1 != null && item1.stackSize == 1 && !item1.getItem().hasContainerItem() && item.isItemEqual(item1)){
								item1.stackSize++;
								
								remaining--;
								if (remaining == 0){
									break;
								}
							}
						}
						if (remaining <= 0){
							inventory.setInventorySlotContents(slot, null);
						}else{
							inventory.decrStackSize(slot, item.stackSize - remaining);
						}
						
						if (this.canCraft()) return;
					}
				}
			}else if (tileEntity instanceof net.minecraftforge.common.ISidedInventory){
				//Forge's implementation of ISidedInventory
				
				net.minecraftforge.common.ISidedInventory inventory = (net.minecraftforge.common.ISidedInventory) tileEntity;
				int startSlot = inventory.getStartInventorySide(side.getOpposite());
				int size = inventory.getSizeInventorySide(side.getOpposite());
				
				for (int slot = startSlot; slot < startSlot + size; slot++){
					if (slot < 0){
						continue;
					}
					
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (item != null && item.stackSize > 0){
						int remaining = item.stackSize;
						for (int slot1 = 0; slot1 < this.getSizeInventory(); slot1++){
							ItemStack item1 = this.getStackInSlot(slot1);
							if (item1 != null && item1.stackSize == 1 && !item1.getItem().hasContainerItem() && item.isItemEqual(item1)){
								item1.stackSize++;
								
								remaining--;
								if (remaining == 0){
									break;
								}
							}
						}
						if (remaining <= 0){
							inventory.setInventorySlotContents(slot, null);
						}else{
							inventory.decrStackSize(slot, item.stackSize - remaining);
						}
						
						if (this.canCraft()) return;
					}
				}
			}else if (tileEntity instanceof IInventory){
				IInventory inventory = Utils.getInventory((IInventory) tileEntity);
				
				for (int slot = 0; slot < inventory.getSizeInventory(); slot++){
					ItemStack item = inventory.getStackInSlot(slot);
					
					if (item != null && item.stackSize > 0){
						int remaining = item.stackSize;
						for (int slot1 = 0; slot1 < this.getSizeInventory(); slot1++){
							ItemStack item1 = this.getStackInSlot(slot1);
							if (item1 != null && item1.stackSize == 1 && !item1.getItem().hasContainerItem() && item.isItemEqual(item1)){
								item1.stackSize++;
								
								remaining--;
								if (remaining == 0){
									break;
								}
							}
						}
						if (remaining <= 0){
							inventory.setInventorySlotContents(slot, null);
						}else{
							inventory.decrStackSize(slot, item.stackSize - remaining);
						}
						
						if (this.canCraft()) return;
					}
				}
			}
		}
	}
	
	public void craft(){
		EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		GameRegistry.onItemCrafted(player, this.craftResult.getStackInSlot(0), this.crafting);
		this.craftResult.getStackInSlot(0).getItem().onCreated(this.craftResult.getStackInSlot(0), this.worldObj, player);
		
		for (int slot = 0; slot < this.getSizeInventory(); slot++){
			ItemStack item = this.getStackInSlot(slot);
			
			if (item != null){
				if (item.getItem().hasContainerItem()){
					ItemStack container = item.getItem().getContainerItemStack(item);
					
					if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()){
						MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, container));
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
			
			if (stack != null && (stack.getItem().hasContainerItem() ? stack.stackSize > 1 && !stack.getItem().getContainerItemStack(stack).isItemEqual(stack) : stack.stackSize <= 1)){
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		Utils.readStacksFromNBT(nbt, "stackList", this);
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
				if (this.canRefillFromNeighbour(from)){
					if (doRemove){
						this.refillFromNeighbour(from);						
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
