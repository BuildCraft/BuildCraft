package buildcraft.factory.tile;

import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.fluid.FluidStorage;
import buildcraft.lib.fluids.SingleUseTank;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

public class TilePump extends TileMiner {
    private SingleUseTank fluidStorage = new SingleUseTank("tank", 16000, this);
    private TreeMap<Integer, Deque<BlockPos>> pumpLayerQueues = new TreeMap<>();

    public void rebuildQueue() {
        pumpLayerQueues.clear();
        BlockPos pumpPos = new BlockPos(pos.getX(), currentPos.getY(), pos.getZ());
        Fluid pumpingFluid = BlockUtils.getFluid(worldObj.getBlockState(pumpPos).getBlock());

        if(pumpingFluid == null) {
//            currentPos = currentPos.down();
            return;
        }

//        if(fluidStorage.canAccept(new FluidStack(pumpingFluid, 1)) && fluidStorage.getFluidFluid() != null) {
//            return;
//        }

        Set<BlockPos> visitedBlocks = new HashSet<>();
        Deque<BlockPos> fluidsFound = new LinkedList<>();

        tryAddToQueue(pumpPos, visitedBlocks, fluidsFound, pumpingFluid);

        while(!fluidsFound.isEmpty()) {
            Deque<BlockPos> fluidsToExpand = fluidsFound;
            fluidsFound = new LinkedList<>();
            for(BlockPos index : fluidsToExpand) {
                tryAddToQueue(index.up(), visitedBlocks, fluidsFound, pumpingFluid);
                tryAddToQueue(index.east(), visitedBlocks, fluidsFound, pumpingFluid);
                tryAddToQueue(index.west(), visitedBlocks, fluidsFound, pumpingFluid);
                tryAddToQueue(index.north(), visitedBlocks, fluidsFound, pumpingFluid);
                tryAddToQueue(index.south(), visitedBlocks, fluidsFound, pumpingFluid);
            }
        }
    }

    public void tryAddToQueue(BlockPos pumpPos, Set<BlockPos> visitedBlocks, Deque<BlockPos> fluidsFound, Fluid pumpingFluid) {
        BlockPos index = new BlockPos(pumpPos);
        if(visitedBlocks.add(index)) {
            if((pumpPos.getX() - pos.getX()) * (pumpPos.getX() - pos.getX()) + (pumpPos.getZ() - pos.getZ()) * (pumpPos.getZ() - pos.getZ()) > 64 * 64) {
                return;
            }
            IBlockState state = worldObj.getBlockState(pumpPos);
            if(BlockUtils.getFluid(state.getBlock()) == pumpingFluid) {
                fluidsFound.add(index);
            }
            if(canDrainBlock(state, pumpPos, pumpingFluid)) {
                getLayerQueue(pumpPos.getY()).add(index);
            }
        }
    }

    private BlockPos getNextIndexToPump(boolean remove) {
        if(pumpLayerQueues.isEmpty()) {
            rebuildQueue();
        }
        Deque<BlockPos> topLayer = null;
        if(!pumpLayerQueues.isEmpty()) {
            topLayer = pumpLayerQueues.lastEntry().getValue();
        }
        if(topLayer != null) {
            if(topLayer.isEmpty()) {
                rebuildQueue();
            }
            while(topLayer.isEmpty() && pumpLayerQueues.size() != 0) {
                pumpLayerQueues.pollLastEntry();
            }
            if(topLayer.isEmpty()) {
                addTube();
                return currentPos.down();
            }
            if(remove) {
                return topLayer.pollLast();
            } else {
                return topLayer.peekLast();
            }
        } else {
            if(currentPos.getY() > 0) {
//                addTube();
                return currentPos;//.down();
            } else {
                return currentPos;
            }
        }
    }

    private boolean canDrainBlock(IBlockState state, BlockPos pos, Fluid fluid) {
        FluidStack fluidStack = BlockUtils.drainBlock(state, worldObj, pos, false);
        if(fluidStack == null || fluidStack.amount <= 0) {
            return false;
        } else {
            return fluidStack.getFluid() == fluid;
        }
    }

    private Deque<BlockPos> getLayerQueue(int layer) {
        Deque<BlockPos> pumpQueue = pumpLayerQueues.get(layer);

        if(pumpQueue == null) {
            pumpQueue = new LinkedList<>();
            pumpLayerQueues.put(layer, pumpQueue);
        }

        return pumpQueue;
    }

    @Override
    protected void initCurrentPos() {
        if(currentPos == null) {
            currentPos = pos.down();
//            currentPos = getNextIndexToPump(true);
        }
    }

    @Override
    public void mine() {
//        System.out.println(currentPos);
//        IBlockState state = worldObj.getBlockState(currentPos);
//        if(!BlockUtils.isFullFluidBlock(state, worldObj, currentPos) && !worldObj.isAirBlock(currentPos)) {
//            this.isComplete = true;
//            return;
//        }
        if(currentPos == null) {
            currentPos = getNextIndexToPump(true);
            return;
        }
        int target = 100000;
        progress += battery.extractPower(0, target - progress);
        if(progress >= target) {
            progress = 0;
            fluidStorage.fill(BlockUtils.drainBlock(worldObj, currentPos, true), true);
            currentPos = getNextIndexToPump(true);
            if(currentPos.getY() < 0) {
                setComplete(true);
            }
        }
    }

    private void addTube() {
        if(currentPos.getY() + 1 == pos.getY()) {
            return;
        }
        worldObj.setBlockState(new BlockPos(pos.getX(), currentPos.getY() + 1, pos.getZ()), getBlockForDown().getDefaultState());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        fluidStorage.deserializeNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("fluid_sotrage", fluidStorage.serializeNBT());
        return nbt;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if(side == Side.SERVER) {
            if(id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if(id == NET_LED_STATUS) {
                fluidStorage.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if(side == Side.CLIENT) {
            if(id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side);
            } else if(id == NET_LED_STATUS) {
                fluidStorage.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + fluidStorage.getDebugString());
    }

    @Override
    protected Block getBlockForDown() {
        return BCFactoryBlocks.tube;
    }

    @SideOnly(Side.CLIENT)
    public float getFluidPercentFilledForRender() {
        float val = fluidStorage.getFluidAmount() / (float) fluidStorage.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    @SideOnly(Side.CLIENT)
    public int getFluidColorForRender() {
        return fluidStorage.getFluidColor();
    }
}
