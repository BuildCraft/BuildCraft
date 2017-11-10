/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IFlexibleRecipeViewable;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.recipes.AssemblyRecipeManager;

public class TileAssemblyTable extends TileLaserTableBase implements IInventory, IFlexibleCrafter, ICommandReceiver {
	public String currentRecipeId = "";
	public IFlexibleRecipe<ItemStack> currentRecipe;
	public HashMap<String, CraftingResult<ItemStack>> plannedOutputIcons = new HashMap<String, CraftingResult<ItemStack>>();
	private HashSet<String> plannedOutput = new HashSet<String>();
	private boolean queuedNetworkUpdate = false;

	public List<CraftingResult<ItemStack>> getPotentialOutputs() {
		List<CraftingResult<ItemStack>> result = new LinkedList<CraftingResult<ItemStack>>();

		for (IFlexibleRecipe<ItemStack> recipe : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			CraftingResult<ItemStack> r = recipe.craft(this, true);

			if (r != null) {
				result.add(r);
			}
		}

		return result;
	}

	private void queueNetworkUpdate() {
		queuedNetworkUpdate = true;
	}

	@Override
	public boolean canUpdate() {
		return !FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	@Override
	public void updateEntity() { // WARNING: run only server-side, see canUpdate()
		super.updateEntity();

		if (queuedNetworkUpdate) {
			sendNetworkUpdate();
			queuedNetworkUpdate = false;
		}

		if (currentRecipe == null) {
			return;
		}

		if (!currentRecipe.canBeCrafted(this)) {
			setNextCurrentRecipe();

			if (currentRecipe == null) {
				return;
			}
		}

		if (getEnergy() >= currentRecipe.craft(this, true).energyCost) {
			if (currentRecipe.canBeCrafted(this)) {
				CraftingResult<ItemStack> result = currentRecipe.craft(this, false);
				setEnergy(Math.max(0, getEnergy() - result.energyCost));
				outputStack(result.crafted.copy(), true);

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
	public void readData(ByteBuf stream) {
		super.readData(stream);
		currentRecipeId = NetworkUtils.readUTF(stream);
		plannedOutput.clear();
		int size = stream.readUnsignedByte();
		for (int i = 0; i < size; i++) {
			plannedOutput.add(NetworkUtils.readUTF(stream));
		}

		// Update plannedOutputIcons
		generatePlannedOutputIcons();

		currentRecipe = AssemblyRecipeManager.INSTANCE.getRecipe(currentRecipeId);
	}

	private void generatePlannedOutputIcons() {
		for (String s : plannedOutput) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(s);
			if (recipe != null) {
				CraftingResult<ItemStack> result = recipe.craft(this, true);
				if (result != null && result.usedItems != null && result.usedItems.size() > 0) {
					plannedOutputIcons.put(s, result);
				} else if (recipe instanceof IFlexibleRecipeViewable) {
					// !! HACK !! TODO !! HACK !!
					Object out = ((IFlexibleRecipeViewable) recipe).getOutput();
					if (out instanceof ItemStack) {
						result = new CraftingResult<ItemStack>();
						result.crafted = (ItemStack) out;
						result.recipe = recipe;
						plannedOutputIcons.put(s, result);
					}
				}
			} else {
				plannedOutput.remove(s);
			}
		}

		for (String s : plannedOutputIcons.keySet().toArray(new String[plannedOutputIcons.size()])) {
			if (!(plannedOutput.contains(s))) {
				plannedOutputIcons.remove(s);
			}
		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		NetworkUtils.writeUTF(stream, currentRecipeId);
		stream.writeByte(plannedOutput.size());
		for (String s : plannedOutput) {
			NetworkUtils.writeUTF(stream, s);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("plannedIds", Constants.NBT.TAG_STRING);

		for (int i = 0; i < list.tagCount(); ++i) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(list.getStringTagAt(i));

			if (recipe != null) {
				plannedOutput.add(recipe.getId());
			}
		}

		if (nbt.hasKey("recipeId")) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(nbt.getString("recipeId"));

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

	public boolean isPlanned(IFlexibleRecipe<ItemStack> recipe) {
		if (recipe == null) {
			return false;
		}

		return plannedOutput.contains(recipe.getId());
	}

	public boolean isAssembling(IFlexibleRecipe<ItemStack> recipe) {
		return recipe != null && recipe == currentRecipe;
	}

	private void setCurrentRecipe(IFlexibleRecipe<ItemStack> recipe) {
		currentRecipe = recipe;

		if (recipe != null) {
			currentRecipeId = recipe.getId();
		} else {
			currentRecipeId = "";
		}

		// Update plannedOutputIcons
		generatePlannedOutputIcons();

		if (worldObj != null && !worldObj.isRemote) {
			queueNetworkUpdate();
		}
	}

	@Override
	public int getRequiredEnergy() {
		if (currentRecipe != null) {
			CraftingResult<ItemStack> result = currentRecipe.craft(this, true);

			if (result != null) {
				return result.energyCost;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public void planOutput(IFlexibleRecipe<ItemStack> recipe) {
		if (recipe != null && !isPlanned(recipe)) {
			plannedOutput.add(recipe.getId());

			if (!isAssembling(currentRecipe) || !isPlanned(currentRecipe)) {
				setCurrentRecipe(recipe);
			}

			queueNetworkUpdate();
		}
	}

	public void cancelPlanOutput(IFlexibleRecipe<ItemStack> recipe) {
		if (isAssembling(recipe)) {
			setCurrentRecipe(null);
		}

		plannedOutput.remove(recipe.getId());

		if (!plannedOutput.isEmpty()) {
			setCurrentRecipe(AssemblyRecipeManager.INSTANCE.getRecipe(plannedOutput.iterator().next()));
		}

		queueNetworkUpdate();
	}

	public void setNextCurrentRecipe() {
		boolean takeNext = false;

		for (String recipeId : plannedOutput) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(recipeId);

			if (recipe == null) {
				continue;
			}

			if (recipe == currentRecipe) {
				takeNext = true;
			} else if (takeNext && recipe.canBeCrafted(this)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		for (String recipeId : plannedOutput) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(recipeId);

			if (recipe == null) {
				continue;
			}

			if (recipe.canBeCrafted(this)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		setCurrentRecipe(null);
	}

	public void rpcSelectRecipe(final String id, final boolean select) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "select", new CommandWriter() {
			public void write(ByteBuf data) {
				NetworkUtils.writeUTF(data, id);
				data.writeBoolean(select);
			}
		}));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && "select".equals(command)) {
			String id = NetworkUtils.readUTF(stream);
			boolean select = stream.readBoolean();

			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(id);

			if (recipe != null) {
				if (select) {
					planOutput(recipe);
				} else {
					cancelPlanOutput(recipe);
				}
			}
		}
	}

	@Override
	public boolean hasWork() {
		return currentRecipe != null;
	}

	@Override
	public boolean canCraft() {
		return hasWork();
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
	public int getCraftingItemStackSize() {
		return getSizeInventory();
	}

	@Override
	public ItemStack getCraftingItemStack(int slotid) {
		return getStackInSlot(slotid);
	}

	@Override
	public ItemStack decrCraftingItemStack(int slotid, int val) {
		return decrStackSize(slotid, val);
	}

	@Override
	public FluidStack getCraftingFluidStack(int tankid) {
		return null;
	}

	@Override
	public FluidStack decrCraftingFluidStack(int tankid, int val) {
		return null;
	}

	@Override
	public int getCraftingFluidStackSize() {
		return 0;
	}
}
