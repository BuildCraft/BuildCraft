/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class PipeItemsWood extends Pipe<PipeTransportItems> {

	@MjBattery (maxCapacity = 64, maxReceivedPerCycle = 64, minimumConsumption = 0)
	public double mjStored = 0;

	protected int standardIconIndex = PipeIconProvider.TYPE.PipeItemsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	private PipeLogicWood logic = new PipeLogicWood(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof IPipeTile) {
				return false;
			}
			if (!(tile instanceof IInventory)) {
				return false;
			}
			if (!PipeManager.canExtractItems(pipe, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord)) {
				return false;
			}
			return true;
		}
	};

	public PipeItemsWood(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		logic.onNeighborBlockChange(blockId);
		super.onNeighborBlockChange(blockId);
	}

	@Override
	public void initialize() {
		logic.initialize();
		super.initialize();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN) {
			return standardIconIndex;
		} else {
			int metadata = container.getBlockMetadata();

			if (metadata == direction.ordinal()) {
				return solidIconIndex;
			} else {
				return standardIconIndex;
			}
		}
	}

	@Override
	public void updateEntity () {
		super.updateEntity();

		if (container.getWorldObj().isRemote) {
			return;
		}

		if (mjStored > 0) {
			if (transport.getNumberOfStacks() < PipeTransportItems.MAX_PIPE_STACKS) {
				extractItems();
			}

			mjStored = 0;
		}
	}

	private void extractItems() {
		int meta = container.getBlockMetadata();

		if (meta > 5) {
			return;
		}

		ForgeDirection side = ForgeDirection.getOrientation(meta);
		TileEntity tile = container.getTile(side);

		if (tile instanceof IInventory) {
			if (!PipeManager.canExtractItems(this, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord)) {
				return;
			}

			IInventory inventory = (IInventory) tile;

			ItemStack[] extracted = checkExtract(inventory, true, side.getOpposite());
			if (extracted == null) {
				return;
			}

			tile.markDirty();

			for (ItemStack stack : extracted) {
				if (stack == null || stack.stackSize == 0) {
					mjStored = mjStored > 1 ? mjStored - 1 : 0;

					continue;
				}

				Position entityPos = new Position(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, side.getOpposite());

				entityPos.moveForwards(0.6);

				TravelingItem entity = makeItem(entityPos.x, entityPos.y, entityPos.z, stack);

				transport.injectItem(entity, entityPos.orientation);
			}
		}
	}

	protected TravelingItem makeItem(double x, double y, double z, ItemStack stack) {
		return TravelingItem.make(x, y, z, stack);
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {
		IInventory inv = InvUtils.getInventory(inventory);
		ItemStack result = checkExtractGeneric(inv, doRemove, from);

		if (result != null) {
			return new ItemStack[]{result};
		}

		return null;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from) {
		return checkExtractGeneric(InventoryWrapper.getWrappedInventory(inventory), doRemove, from);
	}

	public ItemStack checkExtractGeneric(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		if (inventory == null) {
			return null;
		}

		for (int k : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack slot = inventory.getStackInSlot(k);

			if (slot != null && slot.stackSize > 0 && inventory.canExtractItem(k, slot, from.ordinal())) {
				if (doRemove) {
					double energyUsed =  mjStored > slot.stackSize ? slot.stackSize : mjStored;
					mjStored -= energyUsed;

					return inventory.decrStackSize(k, (int) energyUsed);
				} else {
					return slot;
				}
			}
		}

		return null;
	}
}
