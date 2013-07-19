/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.BlockIndex;
import buildcraft.core.EntityBlock;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.liquids.SingleUseTank;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadStream;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TilePump extends TileBuildCraft implements IMachine, IPowerReceptor, IFluidHandler {

	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 16;
	EntityBlock tube;
	private TreeMap<Integer, Deque<BlockIndex>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockIndex>>();
	SingleUseTank tank;
	double tubeY = Double.NaN;
	int aimY = 0;
	private PowerHandler powerHandler;

	public TilePump() {
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();
		tank = new SingleUseTank("tank", MAX_LIQUID);
	}

	private void initPowerProvider() {
		powerHandler.configure(1, 8, 10, 100);
		powerHandler.configurePowerPerdition(1, 100);
	}

	// TODO, manage this by different levels (pump what's above first...)
	@Override
	public void updateEntity() {
		super.updateEntity();

		if (tube == null)
			return;


		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		if (tube.posY - aimY > 0.01) {
			tubeY = tube.posY - 0.01;
			setTubePosition();
			sendNetworkUpdate();
			return;
		}

		if (worldObj.getWorldTime() % 4 != 0)
			return;

		BlockIndex index = getNextIndexToPump(false);

		FluidStack fluidToPump = index != null ? Utils.drainBlock(worldObj, index.x, index.y, index.z, false) : null;
		if (fluidToPump != null) {
			if (isFluidAllowed(fluidToPump.getFluid()) && tank.fill(fluidToPump, false) == fluidToPump.amount) {

				if (powerHandler.useEnergy(10, 10, true) == 10) {
					index = getNextIndexToPump(true);

					if (fluidToPump.getFluid() != FluidRegistry.WATER || BuildCraftCore.consumeWaterSources) {
						Utils.drainBlock(worldObj, index.x, index.y, index.z, true);
					}

					tank.fill(fluidToPump, true);
				}
			}
		} else {
			if (worldObj.getWorldTime() % 128 == 0) {
				// TODO: improve that decision

				initializePumpFromPosition(xCoord, aimY, zCoord);

				if (getNextIndexToPump(false) == null) {
					for (int y = yCoord - 1; y > 0; --y) {
						if (isPumpableFluid(xCoord, y, zCoord)) {
							aimY = y;
							return;
						} else if (!worldObj.isAirBlock(xCoord, y, zCoord)) {
							return;
						}
					}
				}
			}
		}

		FluidStack liquid = tank.getFluid();
		if (liquid != null && liquid.amount >= 0) {
			for (int i = 0; i < 6; ++i) {
				Position p = new Position(xCoord, yCoord, zCoord, ForgeDirection.values()[i]);
				p.moveForwards(1);

				TileEntity tile = worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);

				if (tile instanceof IFluidHandler) {
					int moved = ((IFluidHandler) tile).fill(p.orientation.getOpposite(), liquid, true);
					tank.drain(moved, true);
					if (liquid.amount <= 0) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void initialize() {
		tube = FactoryProxy.proxy.newPumpTube(worldObj);

		if (!Double.isNaN(tubeY)) {
			tube.posY = tubeY;
		} else {
			tube.posY = yCoord;
		}

		tubeY = tube.posY;

		if (aimY == 0) {
			aimY = yCoord;
		}

		setTubePosition();

		worldObj.spawnEntityInWorld(tube);

		if (CoreProxy.proxy.isSimulating(worldObj)) {
			sendNetworkUpdate();
		}
	}

	private BlockIndex getNextIndexToPump(boolean remove) {
		if (pumpLayerQueues.isEmpty())
			return null;

		Deque<BlockIndex> topLayer = pumpLayerQueues.lastEntry().getValue();

		if (topLayer != null) {
			if (topLayer.isEmpty())
				pumpLayerQueues.pollLastEntry();
			if (remove) {
				BlockIndex index = topLayer.removeLast();
				return index;
			}
			return topLayer.peekLast();
		}

		return null;
	}

	private Deque<BlockIndex> getLayerQueue(int layer) {
		Deque<BlockIndex> pumpQueue = pumpLayerQueues.get(layer);
		if (pumpQueue == null) {
			pumpQueue = new LinkedList<BlockIndex>();
			pumpLayerQueues.put(layer, pumpQueue);
		}
		return pumpQueue;
	}

	private void initializePumpFromPosition(int x, int y, int z) {
		Fluid pumpingFluid = getFluid(x, y, z);
		if(pumpingFluid == null)
			return;
		
		if(pumpingFluid != tank.getAcceptedFluid() && tank.getAcceptedFluid() != null)
			return;

		Set<BlockIndex> visitedBlocks = new HashSet<BlockIndex>();
		Deque<BlockIndex> fluidsFound = new LinkedList<BlockIndex>();

		queueForPumping(x, y, z, visitedBlocks, fluidsFound, pumpingFluid);

//		long timeoutTime = System.nanoTime() + 10000;

		while (!fluidsFound.isEmpty()) {
			Deque<BlockIndex> fluidsToExpand = fluidsFound;
			fluidsFound = new LinkedList<BlockIndex>();

			for (BlockIndex index : fluidsToExpand) {
				queueForPumping(index.x + 1, index.y, index.z, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x - 1, index.y, index.z, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x, index.y, index.z + 1, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x, index.y, index.z - 1, visitedBlocks, fluidsFound, pumpingFluid);


				queueForPumping(index.x, index.y + 1, index.z, visitedBlocks, fluidsFound, pumpingFluid);

//				if (System.nanoTime() > timeoutTime)
//					return;
			}
		}
	}

	public void queueForPumping(int x, int y, int z, Set<BlockIndex> visitedBlocks, Deque<BlockIndex> fluidsFound, Fluid pumpingFluid) {
		BlockIndex index = new BlockIndex(x, y, z);
		if (visitedBlocks.add(index)) {
			if ((x - xCoord) * (x - xCoord) + (z - zCoord) * (z - zCoord) > 64 * 64)
				return;

			Fluid fluid = getFluid(x, y, z);
			if (fluid == pumpingFluid) {
				getLayerQueue(y).add(index);
				fluidsFound.add(index);
			}
		}
	}

	private boolean isPumpableFluid(int x, int y, int z) {
		return getFluid(x, y, z) != null;
	}

	private Fluid getFluid(int x, int y, int z) {
		FluidStack fluidStack = Utils.drainBlock(worldObj, x, y, z, false);
		if (fluidStack == null)
			return null;

		return isFluidAllowed(fluidStack.getFluid()) ? fluidStack.getFluid() : null;
	}

	private boolean isFluidAllowed(Fluid fluid) {
		return BuildCraftFactory.pumpDimensionList.isFluidAllowed(fluid, worldObj.provider.dimensionId);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		powerHandler.readFromNBT(data);
		tank.readFromNBT(data);

		aimY = data.getInteger("aimY");
		tubeY = data.getFloat("tubeY");

		initPowerProvider();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		powerHandler.writeToNBT(data);
		tank.writeToNBT(data);

		data.setInteger("aimY", aimY);

		if (tube != null) {
			data.setFloat("tubeY", (float) tube.posY);
		} else {
			data.setFloat("tubeY", yCoord);
		}
	}

	@Override
	public boolean isActive() {
		BlockIndex next = getNextIndexToPump(false);
		return isPumpableFluid(next.x, next.y, next.z);
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public PacketPayload getPacketPayload() {
		PacketPayloadStream payload = new PacketPayloadStream(new PacketPayloadStream.StreamWriter() {
			@Override
			public void writeData(DataOutputStream data) throws IOException {
				data.writeInt(aimY);
				data.writeFloat((float) tubeY);
			}
		});

		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		PacketPayloadStream payload = (PacketPayloadStream) packet.payload;
		DataInputStream data = payload.stream;
		aimY = data.readInt();
		tubeY = data.readFloat();

		setTubePosition();
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		handleUpdatePacket(packet);
	}

	private void setTubePosition() {
		if (tube != null) {
			tube.iSize = Utils.pipeMaxPos - Utils.pipeMinPos;
			tube.kSize = Utils.pipeMaxPos - Utils.pipeMinPos;
			tube.jSize = yCoord - tube.posY;

			tube.setPosition(xCoord + Utils.pipeMinPos, tubeY, zCoord + Utils.pipeMinPos);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		if (tube != null) {
			CoreProxy.proxy.removeEntity(tube);
			tube = null;
			tubeY = Double.NaN;
			aimY = 0;
			pumpLayerQueues.clear();
		}
	}

	@Override
	public boolean manageFluids() {
		return true;
	}

	@Override
	public boolean manageSolids() {
		return false;
	}

	@Override
	public boolean allowAction(IAction action) {
		return false;
	}

	// IFluidHandler implementation.
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		// not acceptable
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource != null && !resource.isFluidEqual(tank.getFluid()))
			return null;
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}
}
