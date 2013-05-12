/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.RedstonePowerFramework;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsWood extends Pipe implements IPowerReceptor {

	private IPowerProvider powerProvider;
	
	protected int standardIconIndex = PipeIconProvider.PipeItemsWood_Standard;
	protected int solidIconIndex = PipeIconProvider.PipeAllWood_Solid;

	protected PipeItemsWood(PipeTransportItems transport, PipeLogic logic, int itemID) {
		super(transport, logic, itemID);

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(50, 1, 64, 1, 64);
		powerProvider.configurePowerPerdition(64, 1);
	}
	
	protected PipeItemsWood(int itemID, PipeTransportItems transport) {
		this(transport, new PipeLogicWood(), itemID);
	}

	public PipeItemsWood(int itemID) {
		this(itemID, new PipeTransportItems());
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
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return solidIconIndex;
			else
				return standardIconIndex;
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

		World w = worldObj;

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

		if (meta > 5)
			return;

		Position pos = new Position(xCoord, yCoord, zCoord, ForgeDirection.getOrientation(meta));
		pos.moveForwards(1);
		TileEntity tile = w.getBlockTileEntity((int) pos.x, (int) pos.y, (int) pos.z);

		if (tile instanceof IInventory) {
			if (!PipeManager.canExtractItems(this, w, (int) pos.x, (int) pos.y, (int) pos.z))
				return;

			ItemStack[] extracted = checkExtract(tile, true, pos.orientation.getOpposite());
			if (extracted == null)
				return;

			for (ItemStack stack : extracted) {
				if (stack == null || stack.stackSize == 0) {
					powerProvider.useEnergy(1, 1, false);
					continue;
				}

				Position entityPos = new Position(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, pos.orientation.getOpposite());

				entityPos.moveForwards(0.6);

				IPipedItem entity = new EntityPassiveItem(w, entityPos.x, entityPos.y, entityPos.z, stack);

				((PipeTransportItems) transport).entityEntering(entity, entityPos.orientation);
			}
		}
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this inventory, null if none. On certain cases, the extractable slot depends on the
	 * position of the pipe.
	 */
	public ItemStack[] checkExtract(TileEntity tile, boolean doRemove, ForgeDirection from) {

		// / ISPECIALINVENTORY
		if (tile instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) tile).extractItem(doRemove, from, (int) powerProvider.getEnergyStored());
			if (stacks != null && doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerProvider.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;
		}else if (tile instanceof net.minecraftforge.common.ISidedInventory) {
			//handles forge's ISidedInvenotry implementation
			
			net.minecraftforge.common.ISidedInventory sidedInv = (net.minecraftforge.common.ISidedInventory) tile;

			int first = sidedInv.getStartInventorySide(from);
			int last = first + sidedInv.getSizeInventorySide(from) - 1;

			ItemStack result = checkExtractGeneric((IInventory) tile, doRemove, from, first, last);

			if (result != null) return new ItemStack[] { result };
		}else if (tile instanceof net.minecraft.inventory.ISidedInventory){
			//handles mojangs's ISidedInventory implementation
			
			net.minecraft.inventory.ISidedInventory sidedInv = (net.minecraft.inventory.ISidedInventory) tile;

			int[] a = sidedInv.getSizeInventorySide(from.ordinal());
			
			if (a != null){
				for (int b : a){
					ItemStack stack = sidedInv.getStackInSlot(b);
					if (stack != null && sidedInv.func_102008_b(b, stack, from.ordinal())){
						if (doRemove){
							return new ItemStack[] { sidedInv.decrStackSize(b, (int) powerProvider.useEnergy(1, stack.stackSize, true)) };
						}else{
							return new ItemStack[] { stack };
						}
					}
				}
			}
			return null;
		}else{
			// This is a generic inventory
			IInventory inv = Utils.getInventory((IInventory) tile);

			ItemStack result = checkExtractGeneric(inv, doRemove, from, 0, inv.getSizeInventory() - 1);

			if (result != null)
				return new ItemStack[] { result };
		}

		return null;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, ForgeDirection from, int start, int stop) {
		for (int k = start; k <= stop; ++k) {
			ItemStack slot = inventory.getStackInSlot(k);

			if (slot != null && slot.stackSize > 0) {
				if (doRemove) {
					return inventory.decrStackSize(k, (int) powerProvider.useEnergy(1, slot.stackSize, true));
				} else {
					return slot;
				}
			}
		}

		return null;
	}

	@Override
	public int powerRequest(ForgeDirection from) {
		return getPowerProvider().getMaxEnergyReceived();
	}

	@Override
	public boolean canConnectRedstone() {
		if (PowerFramework.currentFramework instanceof RedstonePowerFramework)
			return true;
		return super.canConnectRedstone();
	}
}
