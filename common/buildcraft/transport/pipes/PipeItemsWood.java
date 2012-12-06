/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ISidedInventory;

public class PipeItemsWood extends Pipe implements IPowerReceptor {

	private IPowerProvider powerProvider;

	private int baseTexture = 1 * 16 + 0;
	private int plainTexture = 1 * 16 + 15;

	protected PipeItemsWood(int itemID, PipeTransportItems transport) {
		super(transport, new PipeLogicWood(), itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 64, 1, 64);
		powerProvider.configurePowerPerdition(64, 1);
	}

	public PipeItemsWood(int itemID) {
		this(itemID, new PipeTransportItems());
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return baseTexture;
		else {
			if (((PipeLogicWood) logic).direction == direction)
				return plainTexture;
			else
				return baseTexture;
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
		if (powerProvider.getEnergyStored() <= 0)
			return;

		ForgeDirection direction = ((PipeLogicWood) logic).direction;
		if (direction == ForgeDirection.UNKNOWN)
			return;

		TileEntity tile = container.getTile(direction);

		if (tile instanceof IInventory) {
         if (!PipeManager.canExtractItems(this, worldObj, tile.xCoord, tile.yCoord, tile.zCoord))
            return;

         IInventory inventory = (IInventory) tile;

         ItemStack[] extracted = checkExtract(inventory, true, direction.getOpposite());
         if (extracted == null)
            return;

         for(ItemStack stack : extracted) {
            if (stack == null || stack.stackSize == 0) {
               powerProvider.useEnergy(1, 1, false);
                  continue;
            }

            Position entityPos = new Position(tile.xCoord + 0.5, tile.yCoord + Utils.getPipeFloorOf(stack), tile.zCoord + 0.5,
                  direction.getOpposite());
            entityPos.moveForwards(0.5);
            IPipedItem entity = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stack);
            ((PipeTransportItems) transport).entityEntering(entity, entityPos.orientation);
         }
      }
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {
		/// ISPECIALINVENTORY
		if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(doRemove, from, (int) powerProvider.getEnergyStored());
			if (stacks != null && doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerProvider.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;
		}

		if (inventory instanceof ISidedInventory) {
			ISidedInventory sidedInv = (ISidedInventory) inventory;

			int first = sidedInv.getStartInventorySide(from);
			int last = first + sidedInv.getSizeInventorySide(from) - 1;

			IInventory inv = Utils.getInventory(inventory);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, first, last);

			if (result != null)
				return new ItemStack[] { result };
		} else if (inventory.getSizeInventory() == 2) {
			// This is an input-output inventory
			int slotIndex = 0;

			if (from == ForgeDirection.DOWN || from == ForgeDirection.UP)
				slotIndex = 0;
			else
				slotIndex = 1;

			ItemStack slot = inventory.getStackInSlot(slotIndex);

			if (slot != null && slot.stackSize > 0)
				if (doRemove)
					return new ItemStack[] { inventory.decrStackSize(slotIndex, (int) powerProvider.useEnergy(1, slot.stackSize, true)) };
				else
					return new ItemStack[] { slot };
		} else if (inventory.getSizeInventory() == 3) {
			// This is a furnace-like inventory
			int slotIndex = 0;

			if (from == ForgeDirection.UP)
				slotIndex = 0;
			else if (from == ForgeDirection.DOWN)
				slotIndex = 1;
			else
				slotIndex = 2;

			ItemStack slot = inventory.getStackInSlot(slotIndex);

			if (slot != null && slot.stackSize > 0)
				if (doRemove)
					return new ItemStack[] { inventory.decrStackSize(slotIndex, (int) powerProvider.useEnergy(1, slot.stackSize, true)) };
				else
					return new ItemStack[] { slot };
		} else {
			// This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, 0, inv.getSizeInventory() - 1);

			if (result != null)
				return new ItemStack[] { result };
		}

		return null;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from, int start, int stop) {
		for (int k = start; k <= stop; ++k)
			if (inventory.getStackInSlot(k) != null && inventory.getStackInSlot(k).stackSize > 0) {
				ItemStack slot = inventory.getStackInSlot(k);

				if (slot != null && slot.stackSize > 0)
					if (doRemove)
						return inventory.decrStackSize(k, (int) powerProvider.useEnergy(1, slot.stackSize, true));
					else
						return slot;
			}

		return null;
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().getMaxEnergyReceived();
	}

	@Override
	public boolean canConnectRedstone() {
		if(PowerFramework.currentFramework instanceof RedstonePowerFramework)
			return true;
		return super.canConnectRedstone();
	}
}
