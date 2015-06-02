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

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.ResourceIdAssemblyTable;
import buildcraft.core.robots.RobotRegistry;
import buildcraft.core.utils.Utils;

public class TileAssemblyTable extends TileLaserTableBase implements IInventory, IFlexibleCrafter, ICommandReceiver {
	public String currentRecipeId = "";
	public IFlexibleRecipe<ItemStack> currentRecipe;
	private HashSet<String> plannedOutput = new HashSet<String>();

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

	// TODO
	/*@Override
	public boolean canUpdate() {
		return !FMLCommonHandler.instance().getEffectiveSide().isClient();
	}*/

	@Override
	public void update() { // WARNING: run only server-side, see canUpdate()
		super.update();

		if (worldObj.isRemote) {
			return;
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

		if (getEnergy() >= currentRecipe.craft(this, true).energyCost
				&& lastMode != IControllable.Mode.Off) {
			setEnergy(0);

			if (currentRecipe.canBeCrafted(this)) {
				ItemStack remaining = currentRecipe.craft(this, false).crafted.copy();

				EntityRobot robot = RobotRegistry.getRegistry(worldObj)
						.robotTaking(new ResourceIdAssemblyTable(this));

				if (robot != null) {
					remaining = robot.receiveItem(this, remaining);
				}

				if (remaining != null && remaining.stackSize > 0) {
					remaining.stackSize -= Utils
							.addToRandomInventoryAround(worldObj, pos, remaining);
				}

				if (remaining != null && remaining.stackSize > 0) {
					remaining.stackSize -= Utils.addToRandomPipeAround(worldObj, pos, null, remaining);
				}

				if (remaining != null && remaining.stackSize > 0) {
					EntityItem entityitem = new EntityItem(worldObj, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
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
	public void readData(ByteBuf stream) {
		super.readData(stream);
		currentRecipeId = Utils.readUTF(stream);
		plannedOutput.clear();
		int size = stream.readUnsignedByte();
		for (int i = 0; i < size; i++) {
			plannedOutput.add(Utils.readUTF(stream));
		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		Utils.writeUTF(stream, currentRecipeId);
		stream.writeByte(plannedOutput.size());
		for (String s: plannedOutput) {
			Utils.writeUTF(stream, s);
		}

		currentRecipe = AssemblyRecipeManager.INSTANCE.getRecipe(currentRecipeId);
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

		if (worldObj != null && !worldObj.isRemote) {
			sendNetworkUpdate();
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
	}

	public void setNextCurrentRecipe() {
		boolean takeNext = false;

		for (String recipeId : plannedOutput) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(recipeId);

			if (recipe == currentRecipe) {
				takeNext = true;
			} else if (takeNext && recipe.canBeCrafted(this)) {
				setCurrentRecipe(recipe);
				return;
			}
		}

		for (String recipeId : plannedOutput) {
			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(recipeId);

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
				Utils.writeUTF(data, id);
				data.writeBoolean(select);
			}
		}));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && "select".equals(command)) {
			String id = Utils.readUTF(stream);
			boolean select = stream.readBoolean();

			IFlexibleRecipe<ItemStack> recipe = AssemblyRecipeManager.INSTANCE.getRecipe(id);

			if (recipe != null) {
				if (select) {
					planOutput(recipe);
				} else {
					cancelPlanOutput(recipe);
				}
			}

			sendNetworkUpdate();
		}
	}

	@Override
	public boolean hasWork() {
		return currentRecipe != null && super.hasWork();
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

	public String getName() {
		return "tile.assemblyTableBlock.name";
	}
}
