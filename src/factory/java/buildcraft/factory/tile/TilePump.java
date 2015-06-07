/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import io.netty.buffer.ByteBuf;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.EntityBlock;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.TileBuffer;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.SingleUseTank;
import buildcraft.core.lib.fluids.TankUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.factory.BuildCraftFactory;
import buildcraft.factory.FactoryProxy;

public class TilePump extends TileBuildCraft implements IHasWork, IFluidHandler, IRedstoneEngineReceiver {

    public static final int REBUID_DELAY = 512;
    public static int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 16;
    public SingleUseTank tank = new SingleUseTank("tank", MAX_LIQUID, this);

    private EntityBlock tube;
    private TreeMap<Integer, Deque<BlockPos>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockPos>>();
    private double tubeY = Double.NaN;
    private int aimY = 0;

    private SafeTimeTracker timer = new SafeTimeTracker(REBUID_DELAY);
    private int tick = Utils.RANDOM.nextInt(32);
    private int tickPumped = tick - 20;
    private int numFluidBlocksFound = 0;
    private boolean powered = false;

    private int ledState;
    // tick % 16 => min. 16 ticks per network update
    private SafeTimeTracker updateTracker = new SafeTimeTracker(Math.max(16, BuildCraftCore.updateFactor));

    public TilePump() {
        super();
        this.setBattery(new RFBattery(1000, 150, 0));
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (powered) {
            pumpLayerQueues.clear();
            destroyTube();
        } else {
            createTube();
        }

        if (worldObj.isRemote) {
            return;
        }

        if (updateTracker.markTimeIfDelay(worldObj)) {
            sendNetworkUpdate();
        }

        pushToConsumers();

        if (powered) {
            return;
        }

        if (tube == null) {
            return;
        }

        if (tube.posY - aimY > 0.01) {
            tubeY = tube.posY - 0.01;
            setTubePosition();
            sendNetworkUpdate();
            return;
        }

        tick++;

        if (tick % 16 != 0) {
            return;
        }

        BlockPos index = getNextIndexToPump(false);

        FluidStack fluidToPump = index != null ? BlockUtils.drainBlock(worldObj, index.x, index.y, index.z, false) : null;
        if (fluidToPump != null) {
            if (isFluidAllowed(fluidToPump.getFluid()) && tank.fill(fluidToPump, false) == fluidToPump.amount) {
                if (getBattery().useEnergy(100, 100, false) > 0) {
                    if (fluidToPump.getFluid() != FluidRegistry.WATER || BuildCraftCore.consumeWaterSources || numFluidBlocksFound < 9) {
                        index = getNextIndexToPump(true);
                        BlockUtils.drainBlock(worldObj, index.x, index.y, index.z, true);
                    }

                    tank.fill(fluidToPump, true);
                    tickPumped = tick;
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

    public void onNeighborBlockChange(Block block) {
        boolean p = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

        if (powered != p) {
            powered = p;

            if (!worldObj.isRemote) {
                sendNetworkUpdate();
            }
        }
    }

    private boolean isBlocked(BlockPos pos) {
        Material mat = BlockUtils.getBlock(worldObj, pos).getMaterial();

        return mat.blocksMovement();
    }

    private void pushToConsumers() {
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
        }

        TankUtils.pushFluidToConsumers(tank, 400, cache);
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

            if (!worldObj.isRemote) {
                sendNetworkUpdate();
            }
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

    private BlockPos getNextIndexToPump(boolean remove) {
        if (pumpLayerQueues.isEmpty()) {
            if (timer.markTimeIfDelay(worldObj)) {
                rebuildQueue();
            }

            return null;
        }

        Deque<BlockPos> topLayer = pumpLayerQueues.lastEntry().getValue();

        if (topLayer != null) {
            if (topLayer.isEmpty()) {
                pumpLayerQueues.pollLastEntry();
            }

            if (remove) {
                BlockPos index = topLayer.pollLast();
                return index;
            } else {
                return topLayer.peekLast();
            }
        } else {
            return null;
        }
    }

    private Deque<BlockPos> getLayerQueue(int layer) {
        Deque<BlockPos> pumpQueue = pumpLayerQueues.get(layer);

        if (pumpQueue == null) {
            pumpQueue = new LinkedList<BlockPos>();
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
        Fluid pumpingFluid = BlockUtils.getFluid(BlockUtils.getBlock(worldObj, pos));

        if (pumpingFluid == null) {
            return;
        }

        if (pumpingFluid != tank.getAcceptedFluid() && tank.getAcceptedFluid() != null) {
            return;
        }

        Set<BlockPos> visitedBlocks = new HashSet<BlockPos>();
        Deque<BlockPos> fluidsFound = new LinkedList<BlockPos>();

        queueForPumping(pos, visitedBlocks, fluidsFound, pumpingFluid);

        // long timeoutTime = System.nanoTime() + 10000;

        while (!fluidsFound.isEmpty()) {
            Deque<BlockPos> fluidsToExpand = fluidsFound;
            fluidsFound = new LinkedList<BlockPos>();

            for (BlockPos index : fluidsToExpand) {
                queueForPumping(index.x, index.y + 1, index.z, visitedBlocks, fluidsFound, pumpingFluid);
                queueForPumping(index.x + 1, index.y, index.z, visitedBlocks, fluidsFound, pumpingFluid);
                queueForPumping(index.x - 1, index.y, index.z, visitedBlocks, fluidsFound, pumpingFluid);
                queueForPumping(index.x, index.y, index.z + 1, visitedBlocks, fluidsFound, pumpingFluid);
                queueForPumping(index.x, index.y, index.z - 1, visitedBlocks, fluidsFound, pumpingFluid);

                if (pumpingFluid == FluidRegistry.WATER && !BuildCraftCore.consumeWaterSources && numFluidBlocksFound >= 9) {
                    return;
                }

                // if (System.nanoTime() > timeoutTime)
                // return;
            }
        }
    }

    public void queueForPumping(BlockPos pos, Set<BlockPos> visitedBlocks, Deque<BlockPos> fluidsFound, Fluid pumpingFluid) {
        BlockPos index = new BlockPos(pos);
        if (visitedBlocks.add(index)) {
            if ((x - xCoord) * (x - xCoord) + (z - zCoord) * (z - zCoord) > 64 * 64) {
                return;
            }

            Block block = BlockUtils.getBlock(worldObj, pos);

            if (BlockUtils.getFluid(block) == pumpingFluid) {
                fluidsFound.add(index);
            }

            if (canDrainBlock(block, pos, pumpingFluid)) {
                getLayerQueue(y).add(index);
                numFluidBlocksFound++;
            }
        }
    }

    private boolean isPumpableFluid(BlockPos pos) {
        Fluid fluid = BlockUtils.getFluid(BlockUtils.getBlock(worldObj, pos));

        if (fluid == null) {
            return false;
        } else if (!isFluidAllowed(fluid)) {
            return false;
        } else {
            return !(tank.getAcceptedFluid() != null && tank.getAcceptedFluid() != fluid);
        }
    }

    private boolean canDrainBlock(Block block, BlockPos pos, Fluid fluid) {
        if (!isFluidAllowed(fluid)) {
            return false;
        }

        FluidStack fluidStack = BlockUtils.drainBlock(block, worldObj, pos, false);

        if (fluidStack == null || fluidStack.amount <= 0) {
            return false;
        } else {
            return fluidStack.getFluid() == fluid;
        }
    }

    private boolean isFluidAllowed(Fluid fluid) {
        return BuildCraftFactory.pumpDimensionList.isFluidAllowed(fluid, worldObj.provider.dimensionId);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        tank.readFromNBT(data);

        powered = data.getBoolean("powered");

        aimY = data.getInteger("aimY");
        tubeY = data.getFloat("tubeY");
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

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
    public boolean hasWork() {
        BlockPos next = getNextIndexToPump(false);

        if (next != null) {
            return isPumpableFluid(next.x, next.y, next.z);
        } else {
            return false;
        }
    }

    @Override
    public void writeData(ByteBuf buf) {
        buf.writeShort(aimY);
        buf.writeFloat((float) tubeY);
        buf.writeBoolean(powered);
        ledState = ((tick - tickPumped) < 48 ? 16 : 0) | (getBattery().getEnergyStored() * 15 / getBattery().getMaxEnergyStored());
        buf.writeByte(ledState);
    }

    @Override
    public void readData(ByteBuf data) {
        aimY = data.readShort();
        tubeY = data.readFloat();
        powered = data.readBoolean();

        int newLedState = data.readUnsignedByte();
        if (newLedState != ledState) {
            ledState = newLedState;
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }

        setTubePosition();
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
    public void onChunkUnload() {
        super.onChunkUnload();

        if (tube != null) {
            // Remove the entity to stop it from piling up.
            CoreProxy.proxy.removeEntity(tube);
            tube = null;
        }
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public void destroy() {
        pumpLayerQueues.clear();
        destroyTube();
    }

    // IFluidHandler implementation.
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        // not acceptable
        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        } else if (!resource.isFluidEqual(tank.getFluid())) {
            return null;
        } else {
            return drain(from, resource.amount, doDrain);
        }
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return new FluidTankInfo[] { tank.getInfo() };
    }

    @Override
    public boolean canConnectRedstoneEngine(EnumFacing side) {
        return true;
    }

    public int getIconGlowLevel(int renderPass) {
        if (renderPass == 1) { // Red LED
            return ledState & 15;
        } else if (renderPass == 2) { // Green LED
            return (ledState >> 4) > 0 ? 15 : 0;
        } else {
            return -1;
        }
    }
}
