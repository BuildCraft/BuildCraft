package buildcraft.factory.tile;

import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.fluids.SingleUseTank;
import buildcraft.lib.fluids.TankUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

public class TilePump extends TileMiner {
    private SingleUseTank tank = new SingleUseTank("tank", 160000, this); // TODO: remove 1 zero
    private TreeMap<Integer, Deque<BlockPos>> pumpLayerQueues = new TreeMap<>();
    private int timeWithoutFluid = 0;
    private boolean canPump = false;

    public static SpriteHolderRegistry.SpriteHolder TUBE_END_TEXTURE = null;
    public static SpriteHolderRegistry.SpriteHolder TUBE_SIDE_TEXTURE = null;

    private void rebuildQueue() {
        pumpLayerQueues.clear();
        BlockPos pumpPos = new BlockPos(pos.getX(), currentPos.getY(), pos.getZ());
        Fluid pumpingFluid = BlockUtils.getFluid(worldObj.getBlockState(pumpPos).getBlock());

        if(pumpingFluid == null) {
            return;
        }

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

    private void tryAddToQueue(BlockPos pumpPos, Set<BlockPos> visitedBlocks, Deque<BlockPos> fluidsFound, Fluid pumpingFluid) {
        BlockPos index = new BlockPos(pumpPos);
        if(visitedBlocks.add(index)) {
            if((pumpPos.getX() - pos.getX()) * (pumpPos.getX() - pos.getX()) + (pumpPos.getZ() - pos.getZ()) * (pumpPos.getZ() - pos.getZ()) > 64 * 64) {
                return;
            }
            IBlockState state = worldObj.getBlockState(pumpPos);
            if(BlockUtils.getFluid(state.getBlock()) == pumpingFluid && canDrainBlock(state, pumpPos, pumpingFluid)) {
                fluidsFound.add(index);
                getLayerQueue(pumpPos.getY()).add(index);
            }
        }
    }

    private void updatePos() {
        if(pumpLayerQueues.isEmpty() && true) {
            rebuildQueue();
        }
        Deque<BlockPos> topLayer = null;
        if(!pumpLayerQueues.isEmpty()) {
            topLayer = pumpLayerQueues.lastEntry().getValue();
        }
        if(topLayer != null && !topLayer.isEmpty()) {
//            currentPos = topLayer.pollLast();
            BlockPos index = null;
            while(index == null || (index.getX() == pos.getX() && index.getY() == currentPos.getY() && index.getZ() == pos.getZ() && !topLayer.isEmpty())) {
                if(index != null) {
                    topLayer.addFirst(index);
                }
                index = topLayer.pollLast();
            }
//            System.out.println(index);
            currentPos = index;
            canPump = true;
        } else {
            System.out.println(":-(");
            rebuildQueue();
            canPump = false;
        }
    }

//    private BlockPos getNextIndexToPump(boolean remove) {
//        if(pumpLayerQueues.isEmpty()) {
//            rebuildQueue();
//        }
//        Deque<BlockPos> topLayer = null;
//        if(!pumpLayerQueues.isEmpty()) {
//            topLayer = pumpLayerQueues.lastEntry().getValue();
//        }
//        if(topLayer != null) {
//            if(topLayer.isEmpty()) {
//                rebuildQueue();
//            }
//            while(topLayer.isEmpty() && pumpLayerQueues.size() != 0) {
//                topLayer = pumpLayerQueues.pollLastEntry().getValue();
//            }
//            BlockPos index = null;
//            while(index == null || (index.getX() == pos.getX() && index.getZ() == pos.getZ() && currentPos.getY() == index.getY() && topLayer.size() > 0)) {
//                rebuildQueue();
//                if(index != null) {
//                    topLayer.addFirst(index);
//                }
//                index = topLayer.peekLast();
//                if(remove) {
//                    return topLayer.removeLast();
//                }
//            }
//            return index;
//        } else {
//            return currentPos;
//        }
//    }

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
            updatePos();
//            currentPos = getNextIndexToPump(true);
        }
    }

    @Override
    public void update() {
        super.update();

        TankUtils.pushFluidAround(worldObj, pos);
    }

    @Override
    public void mine() {
//        System.out.println(currentPos);
//        IBlockState state = worldObj.getBlockState(currentPos);
//        if(!BlockUtils.isFullFluidBlock(state, worldObj, currentPos) && !worldObj.isAirBlock(currentPos)) {
//            this.isComplete = true;
//            return;
//        }
        isComplete = false;
        if(tank.isFull()) {
            setComplete(true);
            return;
        }
        if(currentPos == null) {
            updatePos();
            return;
        }
        int target = 1000; // TODO: add 2 zeroes
        BlockPos pumpPos = new BlockPos(pos.getX(), currentPos.getY(), pos.getZ());
        Fluid pumpingFluid = BlockUtils.getFluid(worldObj.getBlockState(pumpPos).getBlock());

        if(timeWithoutFluid >= 200) {
            if(worldObj.isAirBlock(pumpPos.down()) || BlockUtils.getFluid(worldObj.getBlockState(pumpPos.down()).getBlock()) != null) {
                timeWithoutFluid = 0;
                currentPos = pumpPos.down();
            } else {
                isComplete = true;
            }
            return;
        }

        if(pumpingFluid == null) {
            timeWithoutFluid++;
            return;
        }

        if(tank.getAcceptedFluid() != pumpingFluid && !tank.isEmpty()) {
            this.setComplete(true);
            return;
        }
        progress += battery.extractPower(0, target - progress);
        if(progress >= target) {
            progress = 0;
//            tank.fill(BlockUtils.drainBlock(worldObj, currentPos, true), true);
            FluidStack drain = BlockUtils.drainBlock(worldObj, currentPos, false);
            if(drain != null && canDrainBlock(worldObj.getBlockState(currentPos), currentPos, drain.getFluid()) && canPump) {
                worldObj.setBlockToAir(currentPos);
                tank.fill(drain, true);
            }
            updatePos();
            if(currentPos.getY() < 0) {
                setComplete(true);
            }
        }
        if(!isComplete) {
            setComplete(false);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.deserializeNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tank", tank.serializeNBT());
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
                tank.writeToBuffer(buffer);
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
                tank.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + tank.getDebugString());
    }

    @SideOnly(Side.CLIENT)
    public float getFluidPercentFilledForRender() {
        float val = tank.getFluidAmount() / (float) tank.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    @SideOnly(Side.CLIENT)
    public int getFluidColorForRender() {
        return tank.getFluidColor();
    }

    // Capabilities

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}
