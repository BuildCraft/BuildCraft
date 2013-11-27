/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeManager;
import buildcraft.transport.TravelingItem;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsWood extends Pipe<PipeTransportItems> implements IPowerReceptor {

	protected PowerHandler powerHandler;
	protected int standardIconIndex = PipeIconProvider.TYPE.PipeItemsWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();
	private PipeLogicWood logic = new PipeLogicWood(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof IPipeTile)
				return false;
			if (!(tile instanceof IInventory))
				return false;
			if (!PipeManager.canExtractItems(pipe, tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord))
				return false;
			return true;
		}
	};

	public PipeItemsWood(int itemID) {
		super(new PipeTransportItems(), itemID);

		powerHandler = new PowerHandler(this, Type.MACHINE);
		powerHandler.configure(1, 64.1f, 1, 64.1f);
		powerHandler.configurePowerPerdition(0, 0);
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
		if (direction == ForgeDirection.UNKNOWN)
			return standardIconIndex;
		else {
			int metadata = container.getBlockMetadata();

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
		if (powerHandler.getEnergyStored() <= 0)
			return;

		if (transport.getNumberOfStacks() < PipeTransportItems.MAX_PIPE_STACKS)
			extractItems();
		powerHandler.setEnergy(0);
	}

	private void extractItems() {
		int meta = container.getBlockMetadata();

		if (meta > 5)
			return;

		ForgeDirection side = ForgeDirection.getOrientation(meta);
		TileEntity tile = container.getTile(side);

		if (tile instanceof IInventory) {
			if (!PipeManager.canExtractItems(this, tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord))
				return;

			IInventory inventory = (IInventory) tile;

			ItemStack[] extracted = checkExtract(inventory, true, side.getOpposite());
			if (extracted == null)
				return;

			tile.onInventoryChanged();

			for (ItemStack stack : extracted) {
				if (stack == null || stack.stackSize == 0) {
					powerHandler.useEnergy(1, 1, true);
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
		return new TravelingItem(x, y, z, stack);
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {

		/* ISPECIALINVENTORY */
		if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(doRemove, from, Math.min((int) powerHandler.getEnergyStored(), PipeTransportItems.MAX_PIPE_ITEMS - transport.getNumberOfItems()));
			if (stacks != null && doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerHandler.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;
		} else {

			IInventory inv = Utils.getInventory(inventory);
			ItemStack result = checkExtractGeneric(inv, doRemove, from);

			if (result != null)
				return new ItemStack[]{result};
		}

		return null;

	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from) {
		return checkExtractGeneric(InventoryWrapper.getWrappedInventory(inventory), doRemove, from);
	}

	public ItemStack checkExtractGeneric(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		if (inventory == null)
			return null;

		for (int k : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			ItemStack slot = inventory.getStackInSlot(k);

			if (slot != null && slot.stackSize > 0 && inventory.canExtractItem(k, slot, from.ordinal())) {
				if (doRemove) {
					return inventory.decrStackSize(k, (int) powerHandler.useEnergy(1, slot.stackSize, true));
				} else {
					return slot;
				}
			}
		}

		return null;
	}
}
