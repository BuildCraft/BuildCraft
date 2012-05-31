package net.minecraft.src.buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Container;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftSilicon;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.AssemblyRecipe;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketIds;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.buildcraft.core.network.TilePacketWrapper;

public class TileAssemblyTable extends TileEntity implements IInventory, IPipeConnection {	

	ItemStack [] items = new ItemStack [12];
	
	LinkedList <AssemblyRecipe> plannedOutput = new LinkedList <AssemblyRecipe> ();
		
	public AssemblyRecipe currentRecipe;
	
	private float currentRequiredEnergy = 0;
	private float energyStored = 0;
	
	public static class SelectionMessage {
		/**
		 *  If true, select, if false, unselect
		 */
		@TileNetworkData public boolean select = true;
		
		/**
		 * Id of the item to be crafted
		 */
		@TileNetworkData public int itemID = 0;
		
		/**
		 * Dmg of the item to be crafted
		 */
		@TileNetworkData public int itemDmg = 0;
	}
	
	public static TilePacketWrapper selectionMessageWrapper = new TilePacketWrapper(SelectionMessage.class);
	
	public LinkedList <AssemblyRecipe> getPotentialOutputs () {
		LinkedList <AssemblyRecipe> result = new LinkedList <AssemblyRecipe> ();
		
		for (AssemblyRecipe recipe : BuildCraftCore.assemblyRecipes) {
			if (recipe.canBeDone (items)) {
				result.add(recipe);
			}
		}
		
		return result;
	}
	
	public void receiveLaserEnergy (float energy) {
		energyStored += energy;
	}
	
	@Override
	public void updateEntity () {
		if (currentRecipe == null)
			return;
		
		if (!currentRecipe.canBeDone(items)) {
			setNextCurrentRecipe();
			
			if (currentRecipe == null)
				return;
		}
		
		if (energyStored >= currentRecipe.energy) {
			energyStored = 0;
			
			if (currentRecipe.canBeDone(items)) {
				
				for (ItemStack in : currentRecipe.input) {
					if(in == null)
						continue; //Optimisation, reduces calculation for a null ingredient
					
					int found = 0; //Amount of ingredient found in inventory
					
					for (int i = 0; i < items.length; ++i) {
						if(items[i] == null) continue; //Broken out of large if statement, increases clarity
						
						if(items[i].isItemEqual(in)) {
							
							int supply = items[i].stackSize;
							int toBeFound = in.stackSize - found;
							
							if(supply >= toBeFound)
								found += decrStackSize(i, toBeFound).stackSize; //Adds the amount of ingredient taken (in this case the total still needed)
							else
								found += decrStackSize(i, supply).stackSize; //Adds the amount of ingredient taken (in this case the total in that slot)
							
							if(found >= in.stackSize)
								break; //Breaks out of the for loop when the required amount of ingredient has been taken
						}
					}
				}
				
				StackUtil stackUtils = new StackUtil(currentRecipe.output.copy());
				
				boolean added = stackUtils.addToRandomInventory(this,
						Orientations.Unknown);

				if (!added || stackUtils.items.stackSize > 0) {
					added = Utils.addToRandomPipeEntry(this,
							Orientations.Unknown, stackUtils.items);
				}
				
				if (!added) {
					EntityItem entityitem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.7, zCoord + 0.5,
							currentRecipe.output.copy());

					worldObj.spawnEntityInWorld(entityitem);
				}
				
				setNextCurrentRecipe();
			}
		}
	}
	
	public float getCompletionRatio (float ratio) {
		if (currentRecipe == null) {
			return 0;
		} else if (energyStored >= currentRecipe.energy){
			return ratio;
		} else {
			return energyStored / currentRecipe.energy * ratio;
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
		ItemStack stack = items [i].splitStack(j);
		if (items [i].stackSize == 0) {
			items [i] = null;
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items [i] = itemstack;
		
		if (currentRecipe == null) {
			setNextCurrentRecipe();
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.items[slot] == null) return null;
		
		ItemStack stackToTake = this.items[slot];
		this.items[slot] = null;
		return stackToTake;
	}

	@Override
	public String getInvName() {
		return "Assembly Table";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		Utils.readStacksFromNBT(nbttagcompound, "items", items);
		
		energyStored = nbttagcompound.getFloat("energyStored");				
		
		NBTTagList list = nbttagcompound.getTagList("planned");

		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound cpt = (NBTTagCompound) list.tagAt(i);

			ItemStack stack = ItemStack.loadItemStackFromNBT(cpt);

			for (AssemblyRecipe r : BuildCraftCore.assemblyRecipes) {
				if (r.output.itemID == stack.itemID
						&& r.output.getItemDamage() == stack.getItemDamage()) {
					plannedOutput.add(r);
				}
			}
		}
		
		if (nbttagcompound.hasKey("recipe")) {
			ItemStack stack = ItemStack.loadItemStackFromNBT(nbttagcompound
					.getCompoundTag("recipe"));
			
			for (AssemblyRecipe r : plannedOutput) {
				if (r.output.itemID == stack.itemID
						&& r.output.getItemDamage() == stack.getItemDamage()) {
					currentRecipe = r;
					break;
				}
			}
		}
	}

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		Utils.writeStacksToNBT(nbttagcompound, "items", items);
    	
    	nbttagcompound.setFloat("energyStored", energyStored);
    	
    	NBTTagList list = new NBTTagList();
    	
    	for (AssemblyRecipe recipe : plannedOutput) {
    		NBTTagCompound cpt = new NBTTagCompound();
    		recipe.output.writeToNBT(cpt);
    		list.appendTag(cpt);
    	}
    	
    	nbttagcompound.setTag("planned", list);
    	
    	if (currentRecipe != null) {
    		NBTTagCompound recipe = new NBTTagCompound();
    		currentRecipe.output.writeToNBT(recipe);
    		nbttagcompound.setTag("recipe", recipe);
    	}
    }
	
	public void cleanPlannedOutput () {
		plannedOutput.clear();
	}
	
	public boolean isPlanned (AssemblyRecipe recipe) {
		if (recipe == null) {
			return false;
		}
		
		for (AssemblyRecipe r : plannedOutput) {
			if (r == recipe) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isAssembling (AssemblyRecipe recipe) {
		return recipe != null && recipe == currentRecipe;		
	}
	
	private void setCurrentRecipe(AssemblyRecipe recipe) {
		this.currentRecipe = recipe;
		if(recipe != null)
			this.currentRequiredEnergy = recipe.energy;
		else
			this.currentRequiredEnergy = 0;
	}
	
	public void planOutput (AssemblyRecipe recipe) {
		if (recipe != null && !isPlanned(recipe)) {
			plannedOutput.add(recipe);
			
			if (!isAssembling(currentRecipe) || !isPlanned(currentRecipe)) {
				setCurrentRecipe(recipe);
			}
		}
	}

	public void cancelPlanOutput(AssemblyRecipe recipe) {
		if (isAssembling(recipe)) {
			setCurrentRecipe(null);
		}
		
		plannedOutput.remove(recipe);		
		
		if (plannedOutput.size() != 0) {
			setCurrentRecipe(plannedOutput.getFirst());
		}
	}
	
	public void setNextCurrentRecipe() {
		boolean takeNext = false;

		for (AssemblyRecipe recipe : plannedOutput) {
			if (recipe == currentRecipe) {
				takeNext = true;
			} else if (takeNext && recipe.canBeDone(items)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		for (AssemblyRecipe recipe : plannedOutput) {
			if (recipe.canBeDone(items)) {
				setCurrentRecipe(recipe);
				return;
			}
		}
		
		setCurrentRecipe(null);
	}

	@Override
	public boolean isPipeConnected(Orientations with) {
		return true;
	}
	
	public void handleSelectionMessage(SelectionMessage message) {
		for (AssemblyRecipe recipe : BuildCraftCore.assemblyRecipes) {
			if (recipe.output.itemID == message.itemID
					&& recipe.output.getItemDamage() == message.itemDmg) {
				if (message.select) {
					planOutput(recipe);
				} else {
					cancelPlanOutput(recipe);
				}
				
				break;
			}
		}
	}

	public void sendSelectionTo (EntityPlayer player) {
		for (AssemblyRecipe r : BuildCraftCore.assemblyRecipes) {
			SelectionMessage message = new SelectionMessage();
			
			message.itemID = r.output.itemID;
			message.itemDmg = r.output.getItemDamage();
			
			if (isPlanned(r)) {
				message.select = true;
			} else {
				message.select = false;
			}
			
			PacketUpdate packet = new PacketUpdate(PacketIds.SELECTION_ASSEMBLY, selectionMessageWrapper.toPayload(xCoord, yCoord, zCoord, message));
			packet.posX = xCoord;
			packet.posY = yCoord;
			packet.posZ = zCoord;
			CoreProxy.sendToPlayers(packet.getPacket(), worldObj,
					(int) player.posX, (int) player.posY, (int) player.posZ, DefaultProps.NETWORK_UPDATE_RANGE,
					mod_BuildCraftSilicon.instance);
		}
	}
	
	/* SMP GUI */
	public void getGUINetworkData(int i, int j) {
		switch(i)
		{
		case 0:
			currentRequiredEnergy = j;
			break;
		case 1:
			energyStored = j;
			break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		iCrafting.updateCraftingInventoryInfo(container, 0, (int)currentRequiredEnergy);
		iCrafting.updateCraftingInventoryInfo(container, 1, (int)energyStored);
	}


}
