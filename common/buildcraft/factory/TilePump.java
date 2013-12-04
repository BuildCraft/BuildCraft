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
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.IAction;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.BlockIndex;
import buildcraft.core.CoreConstants;
import buildcraft.core.EntityBlock;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.fluids.FluidUtils;
import buildcraft.core.fluids.SingleUseTank;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadStream;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import buildcraft.core.utils.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.block.material.Material;
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

	public static int REBUID_DELAY = 512;
	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 16;
	EntityBlock tube;
	private TreeMap<Integer, Deque<BlockIndex>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockIndex>>();
	SingleUseTank tank = new SingleUseTank("tank", MAX_LIQUID, this);
	double tubeY = Double.NaN;
	int aimY = 0;
	private PowerHandler powerHandler;
	private TileBuffer[] tileBuffer = null;
	private SafeTimeTracker timer = new SafeTimeTracker();
	private int tick = Utils.RANDOM.nextInt();
	private int numFluidBlocksFound = 0;
	private boolean powered = false;

	public TilePump() {
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();
	}

	private void initPowerProvider() {
		powerHandler.configure(1, 15, 10, 100);
		powerHandler.configurePowerPerdition(1, 100);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (powered) {
			pumpLayerQueues.clear();
			destroyTube();
		} else
			createTube();

		if (worldObj.isRemote)
			return;

		pushToConsumers();
		
		if(powered)
			return;
		
		if(tube == null)
			return;

		if (tube.posY - aimY > 0.01) {
			tubeY = tube.posY - 0.01;
			setTubePosition();
			sendNetworkUpdate();
			return;
		}

		tick++;
		if (tick % 16 != 0)
			return;

		BlockIndex index = getNextIndexToPump(false);

		FluidStack fluidToPump = index != null ? BlockUtil.drainBlock(worldObj, index.x, index.y, index.z, false) : null;
		if (fluidToPump != null) {
			if (isFluidAllowed(fluidToPump.getFluid()) && tank.fill(fluidToPump, false) == fluidToPump.amount) {

				if (powerHandler.useEnergy(10, 10, true) == 10) {

					if (fluidToPump.getFluid() != FluidRegistry.WATER || BuildCraftCore.consumeWaterSources || numFluidBlocksFound < 9) {
						index = getNextIndexToPump(true);
						BlockUtil.drainBlock(worldObj, index.x, index.y, index.z, true);
					}

					tank.fill(fluidToPump, true);
				}
			}
		} else {
			if (tick % 128 == 0) {
				// TODO: improve that decision

				rebuildQueue();

				if (getNextIndexToPump(false) == null) {
					for (int y = yCoord - 1; y > 0; --y) {
						if (isPumpableFluid(xCoord, y, zCoord)) {
							aimY = y;
							return;
						} else if (isBlocked(xCoord, y, zCoord)) {
							return;
						}
					}
				}
			}
		}
	}

	public void onNeighborBlockChange(int id) {
		boolean p = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		if (powered != p) {
			powered = p;
			if(!worldObj.isRemote)
				sendNetworkUpdate();
		}
	}

	private boolean isBlocked(int x, int y, int z) {
		Material mat = worldObj.getBlockMaterial(x, y, z);
		return mat.blocksMovement();
	}

	private void pushToConsumers() {
		if (tileBuffer == null)
			tileBuffer = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		FluidUtils.pushFluidToConsumers(tank, 400, tileBuffer);
	}

	private TileEntity getTile(ForgeDirection side) {
		if (tileBuffer == null)
			tileBuffer = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		return tileBuffer[side.ordinal()].getTile();
	}

	private void createTube() {
		if (tube == null) {
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

			if (!worldObj.isRemote)
				sendNetworkUpdate();
		}
	}

	private void destroyTube() {
		if (tube != null) {
			CoreProxy.proxy.removeEntity(tube);
			tube = null;
			tubeY = Double.NaN;
			aimY = 0;
		}
	}

	private BlockIndex getNextIndexToPump(boolean remove) {
		if (pumpLayerQueues.isEmpty()) {
			if (timer.markTimeIfDelay(worldObj, REBUID_DELAY)) {
				rebuildQueue();
			}
			return null;
		}

		Deque<BlockIndex> topLayer = pumpLayerQueues.lastEntry().getValue();

		if (topLayer != null) {
			if (topLayer.isEmpty())
				pumpLayerQueues.pollLastEntry();
			if (remove) {
				BlockIndex index = topLayer.pollLast();
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

	public void rebuildQueue() {
		numFluidBlocksFound = 0;
		pumpLayerQueues.clear();
		int x = xCoord;
		int y = aimY;
		int z = zCoord;
		Fluid pumpingFluid = BlockUtil.getFluid(worldObj.getBlockId(x, y, z));
		if (pumpingFluid == null)
			return;

		if (pumpingFluid != tank.getAcceptedFluid() && tank.getAcceptedFluid() != null)
			return;

		Set<BlockIndex> visitedBlocks = new HashSet<BlockIndex>();
		Deque<BlockIndex> fluidsFound = new LinkedList<BlockIndex>();

		queueForPumping(x, y, z, visitedBlocks, fluidsFound, pumpingFluid);

//		long timeoutTime = System.nanoTime() + 10000;

		while (!fluidsFound.isEmpty()) {
			Deque<BlockIndex> fluidsToExpand = fluidsFound;
			fluidsFound = new LinkedList<BlockIndex>();

			for (BlockIndex index : fluidsToExpand) {
				queueForPumping(index.x, index.y + 1, index.z, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x + 1, index.y, index.z, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x - 1, index.y, index.z, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x, index.y, index.z + 1, visitedBlocks, fluidsFound, pumpingFluid);
				queueForPumping(index.x, index.y, index.z - 1, visitedBlocks, fluidsFound, pumpingFluid);

				if (pumpingFluid == FluidRegistry.WATER && !BuildCraftCore.consumeWaterSources && numFluidBlocksFound >= 9)
					return;

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

			int blockId = worldObj.getBlockId(x, y, z);
			if (BlockUtil.getFluid(blockId) == pumpingFluid) {
				fluidsFound.add(index);
			}
			if (canDrainBlock(blockId, x, y, z, pumpingFluid)) {
				getLayerQueue(y).add(index);
				numFluidBlocksFound++;
			}
		}
	}

	private boolean isPumpableFluid(int x, int y, int z) {
		Fluid fluid = BlockUtil.getFluid(worldObj.getBlockId(x, y, z));
		if (fluid == null)
			return false;
		if (!isFluidAllowed(fluid))
			return false;
		if (tank.getAcceptedFluid() != null && tank.getAcceptedFluid() != fluid)
			return false;
		return true;
	}

	private boolean canDrainBlock(int blockId, int x, int y, int z, Fluid fluid) {
		if (!isFluidAllowed(fluid))
			return false;

		FluidStack fluidStack = BlockUtil.drainBlock(blockId, worldObj, x, y, z, false);
		if (fluidStack == null || fluidStack.amount <= 0)
			return false;

		return fluidStack.getFluid() == fluid;
	}

	private boolean isFluidAllowed(Fluid fluid) {
		return BuildCraftFactory.pumpDimensionList.isFluidAllowed(fluid, worldObj.provider.dimensionId);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		powerHandler.readFromNBT(data);
		tank.readFromNBT(data);

		powered = data.getBoolean("powered");

		aimY = data.getInteger("aimY");
		tubeY = data.getFloat("tubeY");

		initPowerProvider();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		powerHandler.writeToNBT(data);
		tank.writeToNBT(data);

		data.setBoolean("powered", powered);

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

		if (next != null) {
			return isPumpableFluid(next.x, next.y, next.z);
		}

		return false;
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
				data.writeBoolean(powered);
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
		powered = data.readBoolean();

		setTubePosition();
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		handleUpdatePacket(packet);
	}

	private void setTubePosition() {
		if (tube != null) {
			tube.iSize = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;
			tube.kSize = CoreConstants.PIPE_MAX_POS - CoreConstants.PIPE_MIN_POS;
			tube.jSize = yCoord - tube.posY;

			tube.setPosition(xCoord + CoreConstants.PIPE_MIN_POS, tubeY, zCoord + CoreConstants.PIPE_MIN_POS);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void validate() {
		tileBuffer = null;
		super.validate();
	}

	@Override
	public void destroy() {
		tileBuffer = null;
		pumpLayerQueues.clear();
		destroyTube();
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
		if (resource == null)
			return null;
		if (!resource.isFluidEqual(tank.getFluid()))
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
