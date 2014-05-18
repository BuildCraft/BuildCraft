/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketNBT;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.AssemblyRecipeManager.AssemblyRecipe;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;

public class TileAssemblyTable extends TileLaserTableBase implements IMachine, IInventory {

	public AssemblyRecipe currentRecipe;
	private Set<AssemblyRecipe> plannedOutput = new LinkedHashSet<AssemblyRecipe>();

	public static class SelectionMessage {

		public boolean select;
		public ItemStack stack;

		public NBTTagCompound getNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("s", select);
			NBTTagCompound itemNBT = new NBTTagCompound();
			stack.writeToNBT(itemNBT);
			nbt.setTag("i", itemNBT);
			return nbt;
		}

		public void fromNBT(NBTTagCompound nbt) {
			select = nbt.getBoolean("s");
			NBTTagCompound itemNBT = nbt.getCompoundTag("i");
			stack = ItemStack.loadItemStackFromNBT(itemNBT);
		}
	}

	public List<AssemblyRecipe> getPotentialOutputs() {
		List<AssemblyRecipe> result = new LinkedList<AssemblyRecipe>();

		for (AssemblyRecipe recipe : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			if (recipe.canBeDone(this)) {
				result.add(recipe);
			}
		}

		return result;
	}

	@Override
	public boolean canUpdate() {
		return !FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	@Override
	public void updateEntity() { // WARNING: run only server-side, see canUpdate()
		super.updateEntity();
		if (currentRecipe == null) {
			return;
		}

		if (!currentRecipe.canBeDone(this)) {
			setNextCurrentRecipe();

			if (currentRecipe == null) {
				return;
			}
		}

		if (getEnergy() >= currentRecipe.getEnergyCost() && lastMode != ActionMachineControl.Mode.Off) {
			setEnergy(0);

			if (currentRecipe.canBeDone(this)) {

				currentRecipe.useItems(this);

				ItemStack remaining = currentRecipe.output.copy();
				remaining.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, remaining);

				if (remaining.stackSize > 0) {
					remaining.stackSize -= Utils.addToRandomPipeAround(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN, remaining);
				}

				if (remaining.stackSize > 0) {
					EntityItem entityitem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.7, zCoord + 0.5, currentRecipe.output.copy());

					worldObj.spawnEntityInWorld(entityitem);
				}

				setNextCurrentRecipe();
			}
		}
	}

	/* IINVENTORY */
	@Override
	public int getSizeInventory() {
		return 12;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		super.setInventorySlotContents(slot, stack);

		if (currentRecipe == null) {
			setNextCurrentRecipe();
		}
	}

	@Override
	public String getInventoryName() {
		return StringUtils.localize("tile.assemblyTableBlock.name");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("planned", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound cpt = list.getCompoundTagAt(i);

			ItemStack stack = ItemStack.loadItemStackFromNBT(cpt);
			if (stack == null) {
				continue;
			}

			for (AssemblyRecipe r : AssemblyRecipeManager.INSTANCE.getRecipes()) {
				if (r.output.getItem() == stack.getItem() && r.output.getItemDamage() == stack.getItemDamage()) {
					plannedOutput.add(r);
					break;
				}
			}
		}

		if (nbt.hasKey("recipe")) {
			ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("recipe"));

			for (AssemblyRecipe r : plannedOutput) {
				if (r.output.getItem() == stack.getItem() && r.output.getItemDamage() == stack.getItemDamage()) {
					setCurrentRecipe(r);
					break;
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList list = new NBTTagList();

		for (AssemblyRecipe recipe : plannedOutput) {
			NBTTagCompound cpt = new NBTTagCompound();
			recipe.output.writeToNBT(cpt);
			list.appendTag(cpt);
		}

		nbt.setTag("planned", list);

		if (currentRecipe != null) {
			NBTTagCompound recipe = new NBTTagCompound();
			currentRecipe.output.writeToNBT(recipe);
			nbt.setTag("recipe", recipe);
		}
	}

	public boolean isPlanned(AssemblyRecipe recipe) {
		if (recipe == null) {
			return false;
		}

		return plannedOutput.contains(recipe);
	}

	public boolean isAssembling(AssemblyRecipe recipe) {
		return recipe != null && recipe == currentRecipe;
	}

	private void setCurrentRecipe(AssemblyRecipe recipe) {
		this.currentRecipe = recipe;
	}

	@Override
	public double getRequiredEnergy() {
		if (currentRecipe != null) {
			return currentRecipe.getEnergyCost();
		}
		return 0;
	}

	public void planOutput(AssemblyRecipe recipe) {
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

		if (!plannedOutput.isEmpty()) {
			setCurrentRecipe(plannedOutput.iterator().next());
		}
	}

	public void setNextCurrentRecipe() {
		boolean takeNext = false;

		for (AssemblyRecipe recipe : plannedOutput) {
			if (recipe == currentRecipe) {
				takeNext = true;
			} else if (takeNext && recipe.canBeDone(this)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		for (AssemblyRecipe recipe : plannedOutput) {
			if (recipe.canBeDone(this)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		setCurrentRecipe(null);
	}

	public void handleSelectionMessage(SelectionMessage message) {
		for (AssemblyRecipe recipe : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			if (recipe.output.isItemEqual(message.stack) && ItemStack.areItemStackTagsEqual(recipe.output, message.stack)) {
				if (message.select) {
					planOutput(recipe);
				} else {
					cancelPlanOutput(recipe);
				}

				break;
			}
		}
	}

	public void sendSelectionTo(EntityPlayer player) {
		for (AssemblyRecipe recipe : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			SelectionMessage message = new SelectionMessage();

			message.stack = recipe.output;

			if (isPlanned(recipe)) {
				message.select = true;
			} else {
				message.select = false;
			}

			PacketNBT packet = new PacketNBT(PacketIds.SELECTION_ASSEMBLY_SEND, message.getNBT(), xCoord, yCoord, zCoord);
			packet.posX = xCoord;
			packet.posY = yCoord;
			packet.posZ = zCoord;
			// FIXME: This needs to be switched over to new synch system.
			BuildCraftSilicon.instance.sendToPlayers(packet, worldObj, (int) player.posX, (int) player.posY, (int) player.posZ,
					DefaultProps.NETWORK_UPDATE_RANGE);
		}
	}

	@Override
	public boolean isActive() {
		return currentRecipe != null && super.isActive();
	}

	@Override
	public boolean canCraft() {
		return isActive();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}
}
