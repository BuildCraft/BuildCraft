package buildcraft.factory.tile;

import java.io.IOException;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.factory.block.BlockFloodGate;
import buildcraft.lib.fluids.Tank;
import buildcraft.lib.fluids.TankUtils;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final EnumFacing[] SIDE_INDEXES = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
    public static final int[] REBUILD_DELAYS = new int[] { 128, 256, 512, 1024, 2048, 4096, 8192, 16384 };
    public static final int NET_FLOOD_GATE = 10;

    private boolean[] sidesBlocked = new boolean[5];
    private final Tank tank = new Tank("tank", 2000, this);
    private int delayIndex = 0;
    private int tick = 120;
    private TreeMap<Integer, Deque<BlockPos>> layerQueues = new TreeMap<>();

    public static int getIndexFromSide(EnumFacing side) {
        return Arrays.binarySearch(SIDE_INDEXES, side);//wat?
    }

    public boolean isSideBlocked(EnumFacing side) {
        return sidesBlocked[getIndexFromSide(side)];
    }

    public void setSideBlocked(EnumFacing side, boolean blocked) {
        sidesBlocked[getIndexFromSide(side)] = blocked;
    }

    public int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    private Deque<BlockPos> getLayerQueue(int layer) {
        Deque<BlockPos> pumpQueue = layerQueues.get(layer);

        if (pumpQueue == null) {
            pumpQueue = new LinkedList<>();
            layerQueues.put(layer, pumpQueue);
        }

        return pumpQueue;
    }

    private void rebuildQueue() {
        layerQueues.clear();

        Set<BlockPos> visitedBlocks = new HashSet<>();
        Deque<BlockPos> blocksFound = new LinkedList<>();

        tryAddToQueue(pos, visitedBlocks, blocksFound);

        while (!blocksFound.isEmpty()) {
            Deque<BlockPos> blocksToExpand = blocksFound;
            blocksFound = new LinkedList<>();

            for (BlockPos index : blocksToExpand) {
                tryAddToQueue(index, visitedBlocks, blocksFound);
            }
        }
    }

    private void tryAddToQueue(BlockPos blockPos, Set<BlockPos> visitedBlocks, Deque<BlockPos> blocksFound) {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side != EnumFacing.UP && !isSideBlocked(side)) {
                BlockPos currentPos = blockPos.offset(side);

                if (currentPos.getY() < 0 || currentPos.getY() > 255) {
                    return;
                }
                if (visitedBlocks.add(currentPos)) {
                    if ((currentPos.getX() - pos.getX()) * (currentPos.getX() - pos.getX()) + (currentPos.getZ() - pos.getZ()) * (currentPos.getZ() - pos.getZ()) > 64 * 64) {
                        return;
                    }

                    IBlockState blockState = world.getBlockState(currentPos);

                    Block block = blockState.getBlock();
                    Fluid fluid = BlockUtil.getFluidWithFlowing(block);

                    boolean isCurrentFluid = this.tank.getFluidType() != null && this.tank.getFluidType() == fluid;

                    if (world.isAirBlock(currentPos) || block instanceof BlockFloodGate || isCurrentFluid) {
                        blocksFound.add(currentPos);
                        if (world.isAirBlock(currentPos) || (isCurrentFluid && blockState.getValue(BlockLiquid.LEVEL) != 0)) {
                            getLayerQueue(currentPos.getY()).addLast(currentPos);
                        }
                    }
                }
            }
        }
    }

    private BlockPos getNext() {
        if (layerQueues.isEmpty()) {
            return null;
        }

        Deque<BlockPos> bottomLayer = layerQueues.firstEntry().getValue();

        if (bottomLayer != null) {
            if (bottomLayer.isEmpty()) {
                bottomLayer = layerQueues.pollFirstEntry().getValue();
            }
            return bottomLayer.pollFirst();
        }

        return null;
    }

    // ITickable

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        TankUtils.pullFluidAround(world, pos);

        tick++;
        if (tick % 16 == 0) {
            FluidStack fluid = tank.drain(1000, false);
            if (fluid != null && fluid.amount == 1000) {
                BlockPos current = getNext();
                if (current != null) {
                    world.setBlockState(current, fluid.getFluid().getBlock().getDefaultState());
                    tank.drain(1000, true);
                    delayIndex = 0;
                }
            }
        }

        if (tick % getCurrentDelay() == 0) {
            delayIndex = Math.min(delayIndex + 1, REBUILD_DELAYS.length - 1);
            rebuildQueue();
        }

        sendNetworkUpdate(NET_FLOOD_GATE); // TODO: optimize
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("fluid = " + tank.getDebugString());
        String[] sides = new String[5];
        for (int i = 0; i < sidesBlocked.length; i++) {
            sides[i] = SIDE_INDEXES[i].toString().toLowerCase() + "(" + sidesBlocked[i] + ")";
        }
        left.add("sides = " + String.join(" ", sides));
        left.add("delay = " + getCurrentDelay());
    }

    // NBT

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt);
        for (int i = 0; i < sidesBlocked.length; i++) {
            nbt.setBoolean("sides_blocked_" + i, sidesBlocked[i]);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tank.writeToNBT(nbt);
        for (int i = 0; i < sidesBlocked.length; i++) {
            sidesBlocked[i] = nbt.getBoolean("sides_blocked_" + i);
        }
        return nbt;
    }

    // Netwokring

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER && id == NET_FLOOD_GATE) {
            tank.writeToBuffer(buffer);
            MessageUtil.writeBooleanArray(buffer, sidesBlocked);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT && id == NET_FLOOD_GATE) {
            tank.readFromBuffer(buffer);
            sidesBlocked = MessageUtil.readBooleanArray(buffer, sidesBlocked.length);
        }
    }

    // Capabilities

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_FLUIDS) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}
