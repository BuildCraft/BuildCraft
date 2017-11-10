/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.StringUtils;

public class TileProgrammingTable extends TileLaserTableBase implements IInventory, ISidedInventory, ICommandReceiver {
	public static final int WIDTH = 6;
	public static final int HEIGHT = 4;

	public String currentRecipeId = "";
	public IProgrammingRecipe currentRecipe;
	public List<ItemStack> options;
	public int optionId;
	private boolean queuedNetworkUpdate = false;

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

		if (this.getStackInSlot(0) == null) {
			currentRecipe = null;
			return;
		}

		if (optionId >= 0 && getEnergy() >= currentRecipe.getEnergyCost(options.get(optionId))) {
			if (currentRecipe.canCraft(this.getStackInSlot(0))) {
				ItemStack remaining = currentRecipe.craft(this.getStackInSlot(0), options.get(optionId));
				if (remaining != null && remaining.stackSize > 0) {
					setEnergy(0);
					decrStackSize(0, remaining.stackSize);
					outputStack(remaining, this, 1, false);
				}
			}
			findRecipe();
		}
	}

	/* IINVENTORY */
	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		super.setInventorySlotContents(slot, stack);

		if (slot == 0) {
			findRecipe();
		}
	}

	@Override
	public String getInventoryName() {
		return StringUtils.localize("tile.programmingTableBlock.name");
	}

	@Override
	public void readData(ByteBuf stream) {
		super.readData(stream);
		currentRecipeId = NetworkUtils.readUTF(stream);
		optionId = stream.readByte();
		updateRecipe();
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		NetworkUtils.writeUTF(stream, currentRecipeId);
		stream.writeByte(optionId);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if (nbt.hasKey("recipeId") && nbt.hasKey("optionId")) {
			currentRecipeId = nbt.getString("recipeId");
			optionId = nbt.getInteger("optionId");
		} else {
			currentRecipeId = null;
		}
		updateRecipe();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if (currentRecipeId != null) {
			nbt.setString("recipeId", currentRecipeId);
			nbt.setByte("optionId", (byte) optionId);
		}
	}

	@Override
	public int getRequiredEnergy() {
		if (hasWork()) {
			return currentRecipe.getEnergyCost(options.get(optionId));
		} else {
			return 0;
		}
	}

	public void findRecipe() {
		String oldId = currentRecipeId;
		currentRecipeId = null;

		if (getStackInSlot(0) != null) {
			for (IProgrammingRecipe recipe : BuildcraftRecipeRegistry.programmingTable.getRecipes()) {
				if (recipe.canCraft(getStackInSlot(0))) {
					currentRecipeId = recipe.getId();
					break;
				}
			}
		}

		if ((oldId != null && currentRecipeId != null && !oldId.equals(currentRecipeId))
				|| (oldId == null && currentRecipeId != null)
				|| (oldId != null && currentRecipeId == null)) {
			optionId = -1;
			updateRecipe();
			queueNetworkUpdate();
		}
	}

	public void updateRecipe() {
		currentRecipe = BuildcraftRecipeRegistry.programmingTable.getRecipe(currentRecipeId);
		if (currentRecipe != null) {
			options = currentRecipe.getOptions(WIDTH, HEIGHT);
		} else {
			options = null;
		}
	}

	public void rpcSelectOption(final int pos) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "select", new CommandWriter() {
			public void write(ByteBuf data) {
				data.writeByte(pos);
			}
		}));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && "select".equals(command)) {
			optionId = stream.readByte();
			if (optionId >= options.size()) {
				optionId = -1;
			} else if (optionId < -1) {
				optionId = -1;
			}

			queueNetworkUpdate();
		}
	}

	@Override
	public boolean hasWork() {
		return currentRecipe != null && optionId >= 0 && this.getStackInSlot(1) == null;
	}

	@Override
	public boolean canCraft() {
		return hasWork();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == 0 || stack == null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[]{0, 1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return slot == 0;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot == 1;
	}
}
