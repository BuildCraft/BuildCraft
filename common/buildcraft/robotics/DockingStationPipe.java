package buildcraft.robotics;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.EnumColor;
import buildcraft.api.gates.IGate;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsWood;

public class DockingStationPipe extends DockingStation implements IRequestProvider {

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
				if (item.isCorrupted()) {
					return 0;
				}

				((PipeTransportItems) ((Pipe) getPipe().getPipe()).transport)
						.injectItem(item, from);
			}
			return stack.stackSize;
		}
	};

	private IPipeTile pipe;

	public DockingStationPipe() {
		// Loading later from NBT - DO NOT TOUCH!
	}

	public DockingStationPipe(IPipeTile iPipe, ForgeDirection side) {
		super(new BlockIndex(iPipe.x(), iPipe.y(), iPipe.z()), side);
		pipe = iPipe;
		world = iPipe.getWorld();
	}

	public IPipeTile getPipe() {
		if (pipe == null) {
			TileEntity tile = world.getTileEntity(x(), y(), z());
			if (tile instanceof IPipeTile) {
				pipe = (IPipeTile) tile;
			}
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
		if (getPipe().getPipeType() != IPipeTile.PipeType.ITEM) {
			return null;
		}

		if (!(getPipe().getPipe() instanceof PipeItemsWood)) {
			return null;
		}

		int meta = ((TileEntity) getPipe()).getBlockMetadata();
		ForgeDirection dir = ForgeDirection.getOrientation(meta);

		TileEntity connectedTile = getPipe().getWorld().getTileEntity(x() + dir.offsetX,
				y() + dir.offsetY, z() + dir.offsetZ);
		if (connectedTile instanceof IInventory) {
			return (IInventory) connectedTile;
		}

		return null;
	}

	@Override
	public IFluidHandler getFluidInput() {
		if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
			return null;
		}

		if (!(getPipe().getPipe() instanceof PipeFluidsWood)) {
			return null;
		}

		int meta = ((TileEntity) getPipe()).getBlockMetadata();
		ForgeDirection dir = ForgeDirection.getOrientation(meta);

		TileEntity connectedTile = getPipe().getWorld().getTileEntity(x() + dir.offsetX,
				y() + dir.offsetY, z() + dir.offsetZ);
		if (connectedTile instanceof IFluidHandler) {
			return (IFluidHandler) connectedTile;
		}

		return null;
	}

	@Override
	public IFluidHandler getFluidOutput() {
		if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
			return null;
		}

		return (IFluidHandler) ((Pipe) getPipe().getPipe()).transport;
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
		return this;
	}

	@Override
	public boolean isInitialized() {
		if (getPipe() == null || getPipe().getPipe() == null) {
			return false;
		}
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

	@Override
	public void onChunkUnload() {
		pipe = null;
	}

	@Override
	public int getRequestsCount() {
		return 127;
	}

	@Override
	public ItemStack getRequest(int slot) {
		ForgeDirection side = ForgeDirection.getOrientation((slot & 0x70) >> 4);
		int action = (slot & 0xc) >> 2;
		int param = slot & 0x3;
		IGate gate = getPipe().getPipe().getGate(side);
		if (gate == null) {
			return null;
		}

		List<IStatement> actions = gate.getActions();
		if (actions.size() <= action) {
			return null;
		}

		if (actions.get(action) != BuildCraftRobotics.actionStationRequestItems) {
			return null;
		}

		List<StatementSlot> activeActions = gate.getActiveActions();

		StatementSlot slotStmt = null;
		for (StatementSlot stmt : activeActions) {
			if (stmt.statement == actions.get(action)) {
				slotStmt = stmt;
				break;
			}
		}
		if (slotStmt == null) {
			return null;
		}
		if (slotStmt.parameters.length <= param) {
			return null;
		}

		if (slotStmt.parameters[param] == null) {
			return null;
		}

		return slotStmt.parameters[param].getItemStack();
	}

	@Override
	public ItemStack offerItem(int slot, ItemStack stack) {
		int consumed = injectablePipe.injectItem(stack, true, side.getOpposite(), null);
		if (stack.stackSize > consumed) {
			ItemStack newStack = stack.copy();
			newStack.stackSize -= consumed;
			return newStack;
		}
		return null;
	}
}
