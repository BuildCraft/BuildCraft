/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.*;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.fluids.Tank;
import buildcraft.core.lib.fluids.TankUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;

import io.netty.buffer.ByteBuf;

public class TileFloodGate extends TileBuildCraft implements IFluidHandler {
    public static final int[] REBUILD_DELAY = new int[8];
    public static final int MAX_LIQUID = FluidContainerRegistry.BUCKET_VOLUME * 2;
    private final TreeMap<Integer, Deque<BlockPos>> pumpLayerQueues = new TreeMap<Integer, Deque<BlockPos>>();
    private final Set<BlockPos> visitedBlocks = new HashSet<BlockPos>();
    private Deque<BlockPos> fluidsFound = new LinkedList<BlockPos>();
    private final Tank tank = new Tank("tank", MAX_LIQUID, this);
    private int rebuildDelay;
    private int tick = Utils.RANDOM.nextInt();
    private boolean powered = false;
    private EnumSet<EnumFacing> blockedSides = EnumSet.noneOf(EnumFacing.class);

    static {
        REBUILD_DELAY[0] = 128;
        REBUILD_DELAY[1] = 256;
        REBUILD_DELAY[2] = 512;
        REBUILD_DELAY[3] = 1024;
        REBUILD_DELAY[4] = 2048;
        REBUILD_DELAY[5] = 4096;
        REBUILD_DELAY[6] = 8192;
        REBUILD_DELAY[7] = 16384;
    }

    public TileFloodGate() {}

    @Override
    public void update() {
        super.update();

        if (worldObj.isRemote) {
            return;
        }

        if (powered) {
            return;
        }

        tick++;
        if (tick % 16 == 0) {
            FluidStack fluidtoFill = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);
            if (fluidtoFill != null && fluidtoFill.amount == FluidContainerRegistry.BUCKET_VOLUME) {
                Fluid fluid = fluidtoFill.getFluid();
                if (fluid == null || !fluid.canBePlacedInWorld()) {
                    return;
                }

                if (fluid == FluidRegistry.WATER && worldObj.provider.getDimensionId() == -1) {
                    tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                    return;
                }

                if (tick % REBUILD_DELAY[rebuildDelay] == 0) {
                    rebuildDelay++;
                    if (rebuildDelay >= REBUILD_DELAY.length) {
                        rebuildDelay = REBUILD_DELAY.length - 1;
                    }
                    rebuildQueue();
                }
                BlockPos index = getNextIndexToFill(true);

                if (index != null && placeFluid(index, fluid)) {
                    tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                    rebuildDelay = 0;
                }
            }
        }
    }

    private boolean placeFluid(BlockPos pos, Fluid fluid) {
        Block block = BlockUtils.getBlockState(worldObj, pos).getBlock();

        if (canPlaceFluidAt(block, pos)) {
            boolean placed;
            Block b = TankUtils.getFluidBlock(fluid, true);

            if (b instanceof BlockFluidBase) {
                BlockFluidBase blockFluid = (BlockFluidBase) b;
                placed = worldObj.setBlockState(pos, blockFluid.getDefaultState(), 3);
            } else {
                placed = worldObj.setBlockState(pos, b.getDefaultState());
            }

            if (placed) {
                queueAdjacent(pos);
                expandQueue();
            }

            return placed;
        }

        return false;
    }

    private BlockPos getNextIndexToFill(boolean remove) {
        if (pumpLayerQueues.isEmpty()) {
            return null;
        }

        Deque<BlockPos> bottomLayer = pumpLayerQueues.firstEntry().getValue();

        if (bottomLayer != null) {
            if (bottomLayer.isEmpty()) {
                pumpLayerQueues.pollFirstEntry();
            }
            if (remove) {
                BlockPos index = bottomLayer.pollFirst();
                return index;
            }
            return bottomLayer.peekFirst();
        }

        return null;
    }

    private Deque<BlockPos> getLayerQueue(int layer) {
        Deque<BlockPos> pumpQueue = pumpLayerQueues.get(layer);
        if (pumpQueue == null) {
            pumpQueue = new LinkedList<BlockPos>();
            pumpLayerQueues.put(layer, pumpQueue);
        }
        return pumpQueue;
    }

    /** Nasty expensive function, don't call if you don't have to. */
    public void rebuildQueue() {
        pumpLayerQueues.clear();
        visitedBlocks.clear();
        fluidsFound.clear();

        queueAdjacent(pos);

        expandQueue();
    }

    private void expandQueue() {
        if (tank.getFluidType() == null) {
            return;
        }
        while (!fluidsFound.isEmpty()) {
            Deque<BlockPos> fluidsToExpand = fluidsFound;
            fluidsFound = new LinkedList<BlockPos>();

            for (BlockPos index : fluidsToExpand) {
                queueAdjacent(index);
            }
        }
    }

    public void queueAdjacent(BlockPos pos) {
        if (tank.getFluidType() == null) {
            return;
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            if (face != EnumFacing.UP && !blockedSides.contains(face)) {
                queueForFilling(pos.offset(face));
            }
        }
    }

    public void queueForFilling(BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() > 255) {
            return;
        }
        if (visitedBlocks.add(pos)) {
            if ((pos.getX() - this.pos.getX()) * (pos.getX() - this.pos.getX()) + (pos.getZ() - this.pos.getZ()) * (pos.getZ() - this.pos.getZ()) > 64
                * 64) {
                return;
            }

            Block block = BlockUtils.getBlockState(worldObj, pos).getBlock();
            if (BlockUtils.getFluid(block) == tank.getFluidType()) {
                fluidsFound.add(pos);
            }
            if (canPlaceFluidAt(block, pos)) {
                getLayerQueue(pos.getY()).addLast(pos);
            }
        }
    }

    private boolean canPlaceFluidAt(Block block, BlockPos pos) {
        return BuildCraftAPI.isSoftBlock(worldObj, pos) && !BlockUtils.isFullFluidBlock(worldObj, pos);
    }

    public void onNeighborBlockChange(Block block) {
        boolean p = worldObj.isBlockIndirectlyGettingPowered(pos) > 0;
        if (powered != p) {
            powered = p;
            if (!p) {
                rebuildQueue();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        tank.readFromNBT(data);
        rebuildDelay = data.getByte("rebuildDelay");
        powered = data.getBoolean("powered");
        blockedSides.clear();
        for (int i = 0; i < 6; i++) {
            boolean blocked = data.getBoolean("blocked[" + i + "]");
            if (blocked) blockedSides.add(EnumFacing.VALUES[i]);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        tank.writeToNBT(data);
        data.setByte("rebuildDelay", (byte) rebuildDelay);
        data.setBoolean("powered", powered);
        for (int i = 0; i < 6; i++) {
            if (blockedSides.contains(EnumFacing.VALUES[i])) {
                data.setBoolean("blocked[" + i + "]", true);
            }
        }
    }

    @Override
    public void readData(ByteBuf stream) {
        byte data = stream.readByte();
        blockedSides.clear();
        for (EnumFacing face : EnumFacing.VALUES) {
            int offset = face.ordinal();
            int isBlocked = (data >> offset) % 2;
            if (isBlocked != 0) blockedSides.add(face);
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        int offset = 0;
        byte data = 0;
        for (EnumFacing face : EnumFacing.VALUES) {
            int isBlocked = blockedSides.contains(face) ? 1 : 0;
            data &= isBlocked << offset;
            offset++;
        }
        stream.writeByte(data);
    }

    public void switchSide(EnumFacing side) {
        if (side != EnumFacing.UP) {
            if (blockedSides.contains(side)) blockedSides.remove(side);
            else blockedSides.add(side);

            rebuildQueue();
            sendNetworkUpdate();
            worldObj.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        destroy();
    }

    @Override
    public void destroy() {
        pumpLayerQueues.clear();
    }

    // IFluidHandler implementation.
    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return new FluidTankInfo[] { tank.getInfo() };
    }

    public boolean isSideBlocked(EnumFacing face) {
        return blockedSides.contains(face);
    }

    @Override
    public IBlockState getBlockState_MIGRATION_ONLY() {
        return BuildCraftFactory.floodGateBlock.getDefaultState();
    }
}
