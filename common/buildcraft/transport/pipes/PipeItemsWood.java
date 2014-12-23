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

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.RFBattery;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class PipeItemsWood extends Pipe<PipeTransportItems> implements IEnergyHandler {
	protected RFBattery battery = new RFBattery(640, 640, 0);
	
	protected int standardIconIndex = PipeIconProvider.TYPE.PipeItemsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	private int ticksSincePull = 0;
	
	private PipeLogicWood logic = new PipeLogicWood(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof IPipeTile) {
				return false;
			}
			if (!(tile instanceof IInventory)) {
				return false;
			}
			if (!PipeManager.canExtractItems(pipe, tile.getWorld(), tile.getPos())) {
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
	public int getIconIndex(EnumFacing direction) {
		if (direction == null) {
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

		if (container.getWorld().isRemote) {
			return;
		}

		ticksSincePull++;
		
		if (shouldTick()) {
			if (transport.getNumberOfStacks() < PipeTransportItems.MAX_PIPE_STACKS) {
				extractItems();
			}

			battery.setEnergy(0);
			ticksSincePull = 0;
		}
	}
	
	private boolean shouldTick() {
		if (battery.getEnergyStored() >= 64 * 10) {
			return true;
		} else {
			return ticksSincePull >= 16 && battery.getEnergyStored() >= 10;
		}
	}

	private void extractItems() {
		int meta = container.getBlockMetadata();

		if (meta > 5) {
			return;
		}

		EnumFacing side = EnumFacing.getFront(meta);
		TileEntity tile = container.getTile(side);

		if (tile instanceof IInventory) {
			if (!PipeManager.canExtractItems(this, tile.getWorld(), tile.getPos())) {
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
					battery.useEnergy(10, 10, false);

					continue;
				}

				Position entityPos = new Position(tile.getPos().add(0.5D, 0.5D, 0.5D), side.getOpposite());

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
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, EnumFacing from) {
		IInventory inv = InvUtils.getInventory(inventory);
		ItemStack result = checkExtractGeneric(inv, doRemove, from);

		if (result != null) {
			return new ItemStack[]{result};
		}

		return null;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, EnumFacing from) {
		return checkExtractGeneric(InventoryWrapper.getWrappedInventory(inventory), doRemove, from);
	}

	public ItemStack checkExtractGeneric(ISidedInventory inventory, boolean doRemove, EnumFacing from) {
		if (inventory == null) {
			return null;
		}

		for (int k : inventory.getSlotsForFace(from)) {
			ItemStack slot = inventory.getStackInSlot(k);

			if (slot != null && slot.stackSize > 0 && inventory.canExtractItem(k, slot, from)) {
				if (doRemove) {
					int stackSize = battery.useEnergy(10, slot.stackSize * 10, false) / 10;
					
					return inventory.decrStackSize(k, stackSize);
				} else {
					return slot;
				}
			}
		}

		return null;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive,
			boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return battery.getMaxEnergyStored();
	}
}
