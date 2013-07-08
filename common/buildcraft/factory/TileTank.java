/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import static net.minecraftforge.common.ForgeDirection.DOWN;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;

public class TileTank extends TileBuildCraft implements ITankContainer {

	public final FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 16);
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
	}

	/* NETWORK */
	@Override
	public PacketPayload getPacketPayload() {
		PacketPayload payload = new PacketPayload(3, 0, 0);
		if (tank.getFluid() != null) {
			payload.intPayload[0] = tank.getFluid().itemID;
			payload.intPayload[1] = tank.getFluid().itemMeta;
			payload.intPayload[2] = tank.getFluid().amount;
		} else {
			payload.intPayload[0] = 0;
			payload.intPayload[1] = 0;
			payload.intPayload[2] = 0;
		}
		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		if (packet.payload.intPayload[0] > 0) {
			FluidStack liquid = new FluidStack(packet.payload.intPayload[0], packet.payload.intPayload[2], packet.payload.intPayload[1]);
			tank.setFluid(liquid);
		} else {
			tank.setFluid(null);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		if (data.hasKey("stored") && data.hasKey("liquidId")) {
			FluidStack liquid = new FluidStack(data.getInteger("liquidId"), data.getInteger("stored"), 0);
			tank.setFluid(liquid);
		} else {
			FluidStack liquid = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("tank"));
			if (liquid != null) {
				tank.setFluid(liquid);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		if (tank.containsValidFluid()) {
			data.setTag("tank", tank.getFluid().writeToNBT(new NBTTagCompound()));
		}
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

	/* ITANKCONTAINER */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return fill(0, resource, doFill);
	}

	@Override
	public int fill(int tankIndex, FluidStack resource, boolean doFill) {
		if (tankIndex != 0 || resource == null) {
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
		return drain(0, maxEmpty, doDrain);
	}

	@Override
	public FluidStack drain(int tankIndex, int maxEmpty, boolean doDrain) {
		TileTank bottom = getBottomTank();
		bottom.hasUpdate = true;
		return bottom.tank.drain(maxEmpty, doDrain);
	}

	@Override
	public IFluidTank[] getTanks(ForgeDirection direction) {
		FluidTank compositeTank = new FluidTank(tank.getCapacity());

		TileTank tile = getBottomTank();

		int capacity = tank.getCapacity();

		if (tile != null && tile.tank.getFluid() != null) {
			compositeTank.setFluid(tile.tank.getFluid().copy());
		} else {
			return new IFluidTank[]{compositeTank};
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
		return new IFluidTank[]{compositeTank};
	}

	@Override
	public IFluidTank getTank(ForgeDirection direction, FluidStack type) {
		if (direction == DOWN && worldObj != null && worldObj.getBlockId(xCoord, yCoord - 1, zCoord) != BuildCraftFactory.tankBlock.blockID) {
			return tank;
		}
		return null;
	}
}
