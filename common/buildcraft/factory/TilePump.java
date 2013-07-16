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
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadArrays;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TilePump extends TileBuildCraft implements IMachine, IPowerReceptor, IFluidHandler {

	public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 16;
	EntityBlock tube;
	private TreeMap<Integer, LinkedList<BlockIndex>> blocksToPump = new TreeMap<Integer, LinkedList<BlockIndex>>();
	FluidTank tank;
	double tubeY = Double.NaN;
	int aimY = 0;
	private PowerHandler powerHandler;

	public TilePump() {
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();
		tank = new FluidTank(MAX_LIQUID);
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

		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			if (tube.posY - aimY > 0.01) {
				tubeY = tube.posY - 0.01;

				setTubePosition();

				if (CoreProxy.proxy.isSimulating(worldObj)) {
					sendNetworkUpdate();
				}

				return;
			}

			if (tank.getFluid() == null || tank.getFluid().amount <= 0) {
				BlockIndex index = getNextIndexToPump(false);

				if (isPumpableFluid(index)) {
					FluidStack liquidToPump = Utils.drainBlock(worldObj, index.i, index.j, index.k, false);

					if (tank.fill(liquidToPump, false) == liquidToPump.amount) {

						if (powerHandler.useEnergy(10, 10, true) == 10) {
							index = getNextIndexToPump(true);

							if (liquidToPump.getFluid() != FluidRegistry.WATER || BuildCraftCore.consumeWaterSources) {
								Utils.drainBlock(worldObj, index.i, index.j, index.k, true);
							}

							tank.fill(liquidToPump, true);

							if (CoreProxy.proxy.isSimulating(worldObj)) {
								sendNetworkUpdate();
							}
						}
					}
				} else {
					if (worldObj.getWorldTime() % 100 == 0) {
						// TODO: improve that decision

						initializePumpFromPosition(xCoord, aimY, zCoord);

						if (getNextIndexToPump(false) == null) {
							for (int y = yCoord - 1; y > 0; --y) {
								if (isFluid(new BlockIndex(xCoord, y, zCoord))) {
									aimY = y;
									return;
								} else if (!worldObj.isAirBlock(xCoord, y, zCoord)) {
									return;
								}
							}
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
		LinkedList<BlockIndex> topLayer = null;

		int topLayerHeight = 0;

		for (Integer layer : blocksToPump.keySet()) {
			if (layer > topLayerHeight && blocksToPump.get(layer).size() != 0) {
				topLayerHeight = layer;
				topLayer = blocksToPump.get(layer);
			}
		}

		if (topLayer != null) {
			if (remove) {
				BlockIndex index = topLayer.pop();

				if (topLayer.size() == 0) {
					blocksToPump.remove(topLayerHeight);
				}

				return index;
			} else
				return topLayer.getLast();
		} else
			return null;
	}

	private void initializePumpFromPosition(int x, int y, int z) {
		int liquidId = 0;

		Set<BlockIndex> markedBlocks = new HashSet<BlockIndex>();
		TreeSet<BlockIndex> lastFound = new TreeSet<BlockIndex>();

		if (!blocksToPump.containsKey(y)) {
			blocksToPump.put(y, new LinkedList<BlockIndex>());
		}

		LinkedList<BlockIndex> pumpList = blocksToPump.get(y);

		liquidId = worldObj.getBlockId(x, y, z);

		if (!isFluid(new BlockIndex(x, y, z)))
			return;

		addToPumpIfFluid(new BlockIndex(x, y, z), markedBlocks, lastFound, pumpList, liquidId);

		long timeoutTime = System.currentTimeMillis() + 1000;

		while (lastFound.size() > 0) {
			TreeSet<BlockIndex> visitIteration = new TreeSet<BlockIndex>(lastFound);
			lastFound.clear();

			for (BlockIndex index : visitIteration) {
				addToPumpIfFluid(new BlockIndex(index.i + 1, index.j, index.k), markedBlocks, lastFound, pumpList, liquidId);
				addToPumpIfFluid(new BlockIndex(index.i - 1, index.j, index.k), markedBlocks, lastFound, pumpList, liquidId);
				addToPumpIfFluid(new BlockIndex(index.i, index.j, index.k + 1), markedBlocks, lastFound, pumpList, liquidId);
				addToPumpIfFluid(new BlockIndex(index.i, index.j, index.k - 1), markedBlocks, lastFound, pumpList, liquidId);

				if (!blocksToPump.containsKey(index.j + 1)) {
					blocksToPump.put(index.j + 1, new LinkedList<BlockIndex>());
				}

				pumpList = blocksToPump.get(index.j + 1);

				addToPumpIfFluid(new BlockIndex(index.i, index.j + 1, index.k), markedBlocks, lastFound, pumpList, liquidId);

				if (System.currentTimeMillis() > timeoutTime)
					return;
			}
		}
	}

	public void addToPumpIfFluid(BlockIndex index, Set<BlockIndex> markedBlocks, TreeSet<BlockIndex> lastFound, LinkedList<BlockIndex> pumpList,
			int liquidId) {

		if (liquidId != worldObj.getBlockId(index.i, index.j, index.k))
			return;

		if (markedBlocks.add(index)) {
			if ((index.i - xCoord) * (index.i - xCoord) + (index.k - zCoord) * (index.k - zCoord) > 64 * 64)
				return;

			if (isPumpableFluid(index)) {
				pumpList.push(index);
			}

			if (isFluid(index)) {
				lastFound.add(index);
			}
		}
	}

	private boolean isPumpableFluid(BlockIndex index) {
		return isFluid(index) && worldObj.getBlockMetadata(index.i, index.j, index.k) == 0;
	}

	private boolean isFluid(BlockIndex index) {
		if (index == null)
			return false;

		FluidStack liquid = Utils.drainBlock(worldObj, index.i, index.j, index.k, false);
		if (liquid == null)
			return false;

		return BuildCraftFactory.pumpDimensionList.isFluidAllowed(liquid, worldObj.provider.dimensionId);
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
		return isPumpableFluid(getNextIndexToPump(false));
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
		PacketPayloadArrays payload = new PacketPayloadArrays(3, 1, 0);
		if (tank.getFluid() != null) {
			payload.intPayload[0] = tank.getFluid().getFluid().getID();
			payload.intPayload[1] = tank.getFluid().amount;
		} else {
			payload.intPayload[0] = 0;
			payload.intPayload[1] = 0;
		}
		payload.intPayload[2] = aimY;
		payload.floatPayload[0] = (float) tubeY;

		return payload;
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) {
		PacketPayloadArrays payload = (PacketPayloadArrays)packet.payload;
		if (payload.intPayload[0] > 0) {
			FluidStack liquid = new FluidStack(FluidRegistry.getFluid(payload.intPayload[0]), payload.intPayload[2]);
			tank.setFluid(liquid);
		} else {
			tank.setFluid(null);
		}

		aimY = payload.intPayload[3];
		tubeY = payload.floatPayload[0];

		setTubePosition();
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) {
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
			blocksToPump.clear();
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
