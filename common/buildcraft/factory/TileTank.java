/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.liquids.Tank;
import buildcraft.core.liquids.TankManager;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadStream;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileTank extends TileBuildCraft implements IFluidHandler {

	public final Tank tank = new Tank("tank", FluidContainerRegistry.BUCKET_VOLUME * 16);
	public final TankManager tankManager = new TankManager(tank);
	public boolean hasUpdate = false;
	public SafeTimeTracker tracker = new SafeTimeTracker();

	/* UPDATING */
	@Override
	public void updateEntity() {
		if (CoreProxy.proxy.isSimulating(worldObj) && hasUpdate && tracker.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
			sendNetworkUpdate();
			hasUpdate = false;
		}

		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			return;
		}

		// Have liquid flow down into tanks below if any.
		if (tank.getFluid() != null) {
			moveFluidBelow();
		}
		// Have liquid flow in adjacent tanks.
		if (tank.getFluid() != null) {
			moveFluidToAdjacent();
		}
	}

	/* NETWORK */
	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayloadStream(new PacketPayloadStream.StreamWriter() {
			@Override
			public void writeData(DataOutputStream data) throws IOException {
				tankManager.writeData(data);
			}
		});
		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		DataInputStream stream = ((PacketPayloadStream) packet.payload).stream;
		tankManager.readData(stream);
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		tankManager.readFromNBT(data);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		tankManager.writeToNBT(data);
	}

	/* HELPER FUNCTIONS */
	/**
	 * @return Last tank block below this one or this one if it is the last.
	 */
	public TileTank getBottomTank() {

		TileTank lastTank = this;

		while (true) {
			TileTank below = getTankBelow(lastTank);
			if (below != null) {
				lastTank = below;
			} else {
				break;
			}
		}

		return lastTank;
	}

	public TileTank getTopTank() {

		TileTank lastTank = this;

		while (true) {
			TileTank above = getTankAbove(lastTank);
			if (above != null) {
				lastTank = above;
			} else {
				break;
			}
		}

		return lastTank;
	}

	public static TileTank getTankBelow(TileTank tile) {
		TileEntity below = tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord - 1, tile.zCoord);
		if (below instanceof TileTank) {
			return (TileTank) below;
		} else {
			return null;
		}
	}

	public static TileTank getTankAbove(TileTank tile) {
		TileEntity above = tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		if (above instanceof TileTank) {
			return (TileTank) above;
		} else {
			return null;
		}
	}

	public void moveFluidBelow() {
		TileTank below = getTankBelow(this);
		if (below == null) {
			return;
		}

		int used = below.tank.fill(tank.getFluid(), true);
		if (used > 0) {
			hasUpdate = true; // not redundant because tank.drain operates on an IFluidTank, not a tile
			below.hasUpdate = true; // redundant because below.fill sets hasUpdate

			tank.drain(used, true);
		}
	}
		
	public static ArrayList<TileTank> getAdjacentTanks(TileTank tile) {
		ArrayList<TileTank> adjacents = new ArrayList<TileTank>();
		if (tile.tank.getFluid() != null)
			fillWithAdjacentTanks(tile, adjacents, tile.tank.getFluid());
		return adjacents;
	}

	/**
	 * Recursive function, must only be called by getAdjacentTanks(TileTank tile)
	 */
	private static void fillWithAdjacentTanks(TileTank tile, ArrayList<TileTank> result, FluidStack fluid) {
		result.add(tile);
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			if (direction == ForgeDirection.DOWN || direction == ForgeDirection.UP)
				continue;
			TileEntity other = tile.worldObj.getBlockTileEntity(tile.xCoord + direction.offsetX, tile.yCoord, tile.zCoord + direction.offsetZ);
			if (other instanceof TileTank && !result.contains(other) && (((TileTank) other).tank.isEmpty() || ((TileTank) other).tank.getFluid().isFluidEqual(fluid))) {
				fillWithAdjacentTanks((TileTank) other, result, fluid);
			}
		}
	}

	public void moveFluidToAdjacent() {
		FluidStack fluid = tank.getFluid();
		if (fluid == null)
			return;
		ArrayList<TileTank> adjacents = getAdjacentTanks(this); 
		if(adjacents.size() <= 1) // Note that this tank will be contained in the list too
			return;
		
		int totalAmount = 0;
		for(TileTank other : adjacents)
			totalAmount += other.tank.getFluidAmount();

		int splitAmount = totalAmount / adjacents.size();
		int balance = 0; // Prevent creation or destruction of fluid cause of Euclidean division 
		for(TileTank other : adjacents)
		{
			if(other.tank.getFluidAmount() < splitAmount)
				balance += other.tank.fill(new FluidStack(fluid, splitAmount - other.tank.getFluidAmount()), true);
			else if(other.tank.getFluidAmount() > splitAmount)
				balance -= other.tank.drain(other.tank.getFluidAmount() - splitAmount, true).amount;
			else
				continue;
			other.hasUpdate = true;
		}
		if(balance > 0)
			tank.drain(balance, true);
		else if (balance < 0)
			tank.fill(new FluidStack(fluid, -balance), true);
		else
			return;
		hasUpdate = true;
	}

	/* ITANKCONTAINER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource == null) {
			return 0;
		}

		resource = resource.copy();
		int totalUsed = 0;
		TileTank tankToFill = getBottomTank();

		FluidStack liquid = tankToFill.tank.getFluid();
		if (liquid != null && liquid.amount > 0 && !liquid.isFluidEqual(resource)) {
			return 0;
		}

		while (tankToFill != null && resource.amount > 0) {
			int used = tankToFill.tank.fill(resource, doFill);
			resource.amount -= used;
			if (used > 0) {
				tankToFill.hasUpdate = true;
			}

			totalUsed += used;
			tankToFill = getTankAbove(tankToFill);
		}
		return totalUsed;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxEmpty, boolean doDrain) {
		TileTank bottom = getBottomTank();
		bottom.hasUpdate = true;
		return bottom.tank.drain(maxEmpty, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource != null && !resource.isFluidEqual(tank.getFluid()))
			return null;
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction) {
		FluidTank compositeTank = new FluidTank(tank.getCapacity());

		TileTank tile = getBottomTank();

		int capacity = tank.getCapacity();

		if (tile != null && tile.tank.getFluid() != null) {
			compositeTank.setFluid(tile.tank.getFluid().copy());
		} else {
			return new FluidTankInfo[]{compositeTank.getInfo()};
		}

		tile = getTankAbove(tile);

		while (tile != null) {

			FluidStack liquid = tile.tank.getFluid();
			if (liquid == null || liquid.amount == 0) {
				// NOOP
			} else if (!compositeTank.getFluid().isFluidEqual(liquid)) {
				break;
			} else {
				compositeTank.getFluid().amount += liquid.amount;
			}

			capacity += tile.tank.getCapacity();
			tile = getTankAbove(tile);
		}

		compositeTank.setCapacity(capacity);
		return new FluidTankInfo[]{compositeTank.getInfo()};
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}
}
