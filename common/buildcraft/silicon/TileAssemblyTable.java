/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.NetworkData;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.core.IMachine;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.utils.StringUtils;
import buildcraft.core.utils.Utils;

public class TileAssemblyTable extends TileLaserTableBase implements IMachine, IInventory {

	@NetworkData
	private HashSet<String> plannedOutput = new HashSet<String>();

	@NetworkData
	public String currentRecipeId = "";

	public IFlexibleRecipe currentRecipe;

	public List<CraftingResult> getPotentialOutputs() {
		List<CraftingResult> result = new LinkedList<CraftingResult>();

		for (IFlexibleRecipe recipe : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			CraftingResult r = recipe.craftPreview(this, null);

			if (r != null) {
				result.add(r);
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

		if (!currentRecipe.canBeCrafted(this, null)) {
			setNextCurrentRecipe();

			if (currentRecipe == null) {
				return;
			}
		}

		if (getEnergy() >= currentRecipe.craftPreview(this, null).energyCost
				&& lastMode != ActionMachineControl.Mode.Off) {
			setEnergy(0);

			if (currentRecipe.canBeCrafted(this, null)) {
				ItemStack remaining = (ItemStack) currentRecipe.craft(this, null).crafted;
				remaining.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, remaining);

				if (remaining.stackSize > 0) {
					remaining.stackSize -= Utils.addToRandomPipeAround(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN, remaining);
				}

				if (remaining.stackSize > 0) {
					EntityItem entityitem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.7, zCoord + 0.5,
							remaining);

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

		NBTTagList list = nbt.getTagList("plannedIds", Constants.NBT.TAG_STRING);

		for (int i = 0; i < list.tagCount(); ++i) {
			IFlexibleRecipe recipe = AssemblyRecipeManager.INSTANCE.getRecipe(list.getStringTagAt(i));

			if (recipe != null) {
				plannedOutput.add(recipe.getId());
			}
		}

		if (nbt.hasKey("recipeId")) {
			IFlexibleRecipe recipe = AssemblyRecipeManager.INSTANCE.getRecipe(nbt.getString("recipeId"));

			if (recipe != null) {
				setCurrentRecipe(recipe);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList list = new NBTTagList();

		for (String recipe : plannedOutput) {
			list.appendTag(new NBTTagString(recipe));
		}

		nbt.setTag("plannedIds", list);

		if (currentRecipe != null) {
			nbt.setString("recipeId", currentRecipe.getId());
		}
	}

	public boolean isPlanned(IFlexibleRecipe recipe) {
		if (recipe == null) {
			return false;
		}

		return plannedOutput.contains(recipe.getId());
	}

	public boolean isAssembling(IFlexibleRecipe recipe) {
		return recipe != null && recipe == currentRecipe;
	}

	private void setCurrentRecipe(IFlexibleRecipe recipe) {
		currentRecipe = recipe;

		if (recipe != null) {
			currentRecipeId = recipe.getId();
		} else {
			currentRecipeId = "";
		}

		if (!worldObj.isRemote) {
			sendNetworkUpdate();
		}
	}

	@Override
	public double getRequiredEnergy() {
		if (currentRecipe != null) {
			CraftingResult result = currentRecipe.craftPreview(this, null);

			if (result != null) {
				return result.energyCost;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public void planOutput(IFlexibleRecipe recipe) {
		if (recipe != null && !isPlanned(recipe)) {
			plannedOutput.add(recipe.getId());

			if (!isAssembling(currentRecipe) || !isPlanned(currentRecipe)) {
				setCurrentRecipe(recipe);
			}
		}
	}

	public void cancelPlanOutput(IFlexibleRecipe recipe) {
		if (isAssembling(recipe)) {
			setCurrentRecipe(null);
		}

		plannedOutput.remove(recipe.getId());

		if (!plannedOutput.isEmpty()) {
			setCurrentRecipe(AssemblyRecipeManager.INSTANCE.getRecipe(plannedOutput.iterator().next()));
		}
	}

	public void setNextCurrentRecipe() {
		boolean takeNext = false;

		for (String recipeId : plannedOutput) {
			IFlexibleRecipe recipe = AssemblyRecipeManager.INSTANCE.getRecipe(recipeId);

			if (recipe == currentRecipe) {
				takeNext = true;
			} else if (takeNext && recipe.canBeCrafted(this, null)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		for (String recipeId : plannedOutput) {
			IFlexibleRecipe recipe = AssemblyRecipeManager.INSTANCE.getRecipe(recipeId);

			if (recipe.canBeCrafted(this, null)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		setCurrentRecipe(null);
	}

	public void rpcSelectRecipe(String id, boolean select) {
		RPCHandler.rpcServer(this, "selectRecipe", id, select);
	}

	@RPC(RPCSide.SERVER)
	private void selectRecipe(String id, boolean select) {
		IFlexibleRecipe recipe = AssemblyRecipeManager.INSTANCE.getRecipe(id);

		if (recipe != null) {
			if (select) {
				planOutput(recipe);
			} else {
				cancelPlanOutput(recipe);
			}
		}

		sendNetworkUpdate();
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

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		currentRecipe = AssemblyRecipeManager.INSTANCE.getRecipe(currentRecipeId);
	}

}
