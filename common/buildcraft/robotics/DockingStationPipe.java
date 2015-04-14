package buildcraft.robotics;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.EnumColor;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.inventory.InventoryConcatenator;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.ActionIterator;

public class DockingStationPipe extends DockingStation {

	private IInjectable injectablePipe = new IInjectable() {
		@Override
		public boolean canInjectItems(ForgeDirection from) {
			return true;
		}

		@Override
		public int injectItem(ItemStack stack, boolean doAdd, ForgeDirection from, EnumColor color) {
			if (doAdd) {
				float cx = x() + 0.5F + 0.2F * side().offsetX;
				float cy = y() + 0.5F + 0.2F * side().offsetY;
				float cz = z() + 0.5F + 0.2F * side().offsetZ;
				TravelingItem item = TravelingItem.make(cx, cy, cz, stack);

				((PipeTransportItems) ((Pipe) getPipe().getPipe()).transport)
						.injectItem(item, from);
			}
			return stack.stackSize;
		}
	};

	private IPipeTile pipe;

	public DockingStationPipe() {
		// Loading later from NBT
	}

	public DockingStationPipe(IPipeTile iPipe, ForgeDirection side) {
		super(new BlockIndex(iPipe.x(), iPipe.y(), iPipe.z()), side);
		pipe = iPipe;
		world = iPipe.getWorld();
	}

	public IPipeTile getPipe() {
		if (pipe == null) {
			pipe = (IPipeTile) world.getTileEntity(x(), y(), z());
		}

		if (pipe == null || ((TileEntity) pipe).isInvalid()) {
			// Inconsistency - remove this pipe from the registry.
			RobotManager.registryProvider.getRegistry(world).removeStation(this);
			pipe = null;
		}

		return pipe;
	}

	@Override
	public Iterable<StatementSlot> getActiveActions() {
		return new ActionIterator(getPipe().getPipe());
	}

	@Override
	public IInjectable getItemOutput() {
		if (getPipe().getPipeType() != IPipeTile.PipeType.ITEM) {
			return null;
		}

		return injectablePipe;
	}

	@Override
	public IInventory getItemInput() {
		InventoryConcatenator inv = InventoryConcatenator.make();
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity nearbyTile = getPipe().getWorld().getTileEntity(x() + dir.offsetX,
					y() + dir.offsetY, z() + dir.offsetZ);
			if (nearbyTile instanceof IInventory) {
				inv = inv.add((IInventory) nearbyTile);
			}
		}
		if (inv.getSizeInventory() > 0) {
			return inv;
		} else {
			return null;
		}
	}

	@Override
	public IFluidHandler getFluidInput() {
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity nearbyTile = getPipe().getWorld().getTileEntity(x() + dir.offsetX,
					y() + dir.offsetY, z() + dir.offsetZ);
			if (nearbyTile instanceof IFluidHandler) {
				return (IFluidHandler) nearbyTile;
			}
		}
		return null;
	}

	@Override
	public IFluidHandler getFluidOutput() {
		if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
			return null;
		}

		return (IFluidHandler) getPipe();
	}

	@Override
	public boolean providesPower() {
		return getPipe().getPipeType() == IPipeTile.PipeType.POWER;
	}

	@Override
	public IRequestProvider getRequestProvider() {
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity nearbyTile = getPipe().getWorld().getTileEntity(x() + dir.offsetX,
					y() + dir.offsetY, z() + dir.offsetZ);
			if (nearbyTile instanceof IRequestProvider) {
				return (IRequestProvider) nearbyTile;
			}
		}
		return null;
	}

	@Override
	public boolean isInitialized() {
		return ((Pipe) getPipe().getPipe()).isInitialized();
	}

	@Override
	public boolean take(EntityRobotBase robot) {
		boolean result = super.take(robot);
		if (result) {
			getPipe().scheduleRenderUpdate();
		}
		return result;
	}

	@Override
	public boolean takeAsMain(EntityRobotBase robot) {
		boolean result = super.takeAsMain(robot);
		if (result) {
			getPipe().scheduleRenderUpdate();
		}
		return result;
	}

	@Override
	public void unsafeRelease(EntityRobotBase robot) {
		super.unsafeRelease(robot);
		if (robotTaking() == null) {
			getPipe().scheduleRenderUpdate();
		}
	}

}
