package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.block.BlockFloodGate;
import buildcraft.factory.client.render.AdvDebuggerFloodGate;

public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final EnumFacing[] SIDE_INDEXES = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
    public static final int[] REBUILD_DELAYS = new int[] { 128, 256, 512, 1024, 2048, 4096, 8192, 16384 };

    private boolean[] sidesBlocked = new boolean[5];
    private final Tank tank = new Tank("tank", 2000, this);
    private int delayIndex = 0;
    private int tick = 120;
    private final TreeMap<Integer, Deque<BlockPos>> layerQueues = new TreeMap<>();

    /** Used for debugging on the client with {@link IAdvDebugTarget} */
    public final TreeMap<Integer, Deque<BlockPos>> clientLayerQueues = new TreeMap<>();

    public TileFloodGate() {
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);
    }

    public static int getIndexFromSide(EnumFacing side) {
        return Arrays.binarySearch(SIDE_INDEXES, side);
    }

    public boolean isSideBlocked(EnumFacing side) {
        return sidesBlocked[getIndexFromSide(side)];
    }

    public void setSideBlocked(EnumFacing side, boolean blocked) {
        sidesBlocked[getIndexFromSide(side)] = blocked;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    public int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    private Deque<BlockPos> getLayerQueue(int layer) {
        return layerQueues.computeIfAbsent(layer, k -> new LinkedList<>());
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

                    boolean isCurrentFluid = this.tank.getFluidType() != null && fluid != null && Objects.equals(this.tank.getFluidType().getName(), fluid.getName());

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

        FluidUtilBC.pullFluidAround(world, pos, tank);

        tick++;
        if (tick % 16 == 0) {
            FluidStack fluid = tank.drain(1000, false);
            if (fluid != null && fluid.amount == 1000) {
                BlockPos current = getNext();
                if (current != null && world.isAirBlock(current)) {
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
    }

    // IDebuggable

    @Override
    @SideOnly(Side.CLIENT)
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

    @Override
    @SideOnly(Side.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerFloodGate(this);
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
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                // tank.writeToBuffer(buffer);
                MessageUtil.writeBooleanArray(buffer, sidesBlocked);
            } else if (id == NET_ADV_DEBUG) {
                buffer.writeInt(layerQueues.size());
                for (Entry<Integer, Deque<BlockPos>> entry : layerQueues.entrySet()) {
                    Integer key = entry.getKey();
                    Deque<BlockPos> positions = entry.getValue();
                    buffer.writeInt(key);
                    buffer.writeInt(positions.size());
                    for (BlockPos p : positions) {
                        BlockPos diff = p.subtract(getPos());
                        buffer.writeByte(diff.getX());
                        buffer.writeByte(diff.getZ());
                    }
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                // tank.readFromBuffer(buffer);
                boolean[] read = MessageUtil.readBooleanArray(buffer, sidesBlocked.length);
                if (!Arrays.equals(read, sidesBlocked)) {
                    sidesBlocked = read;
                    redrawBlock();
                }
            } else if (id == NET_ADV_DEBUG) {
                clientLayerQueues.clear();
                int count = buffer.readInt();
                for (int i = 0; i < count; i++) {
                    int key = buffer.readInt();
                    int values = buffer.readInt();
                    Deque<BlockPos> positions = new ArrayDeque<>(values);
                    for (int j = 0; j < values; j++) {
                        int x = getPos().getX() + buffer.readByte();
                        int z = getPos().getZ() + buffer.readByte();
                        positions.add(new BlockPos(x, key, z));
                    }
                    clientLayerQueues.put(key, positions);
                }
            }
        }
    }
}
