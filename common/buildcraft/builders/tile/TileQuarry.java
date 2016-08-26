package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.mj.types.MachineType;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.core.Box;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.BoxIterator;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.mj.MjReciverBatteryWrapper;
import buildcraft.lib.tile.TileBCInventory_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class TileQuarry extends TileBCInventory_Neptune implements ITickable, IDebuggable {
    private final MjBattery battery;
    private final MjCapabilityHelper mjCapHelper;
    private final Box box = new Box();
    private BlockPos min;
    private BlockPos max;
    private BoxIterator boxIterator;
    private Task currentTask = null;
    public final IItemHandlerModifiable invFrames = addInventory("frames", 9, ItemHandlerManager.EnumAccess.NONE, EnumPipePart.VALUES);

    public TileQuarry() {
        battery = new MjBattery(1600L * MjAPI.MJ);
        mjCapHelper = new MjCapabilityHelper(new MjReciverBatteryWrapper(battery, MachineType.QUARRY));
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if(placer.worldObj.isRemote) {
            return;
        }
        EnumFacing facing = worldObj.getBlockState(getPos()).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockPos areaPos = getPos().offset(facing.getOpposite());
        TileEntity tile = worldObj.getTileEntity(areaPos);
        if(tile instanceof IAreaProvider) {
            IAreaProvider provider = (IAreaProvider) tile;
            box.reset();
            min = provider.min();
            max = provider.max();
            box.setMin(new BlockPos(min.getX(), 0, min.getZ()));
            box.setMax(new BlockPos(max.getX(), min.getY() - 1, max.getZ()));
            provider.removeFromWorld();
        }
    }

    @Override
    public void update() {
        if(worldObj.isRemote) {
            return;
        }

         if (!battery.isFull()) {
             // test with the output of a stone engine
             battery.addPower(MjAPI.MJ); // remove this
         }

        if(min == null || max == null) {
            return;
        }

        if(currentTask != null) {
            if(currentTask.addEnergy(battery.extractPower(0, currentTask.getTarget() - currentTask.getEnergy()))) {
                currentTask = null;
            }
            return;
        }

        for(int i = 0; i < 2; i++) { // 2 iterations: first is removing blocks, second is adding frames
            for(int x = min.getX(); x <= max.getX(); x++) {
                for(int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, min.getY(), z);
                    boolean shouldBeFrame = x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ();
                    Block block = worldObj.getBlockState(pos).getBlock();
                    if(i == 0) {
                        if((block != Blocks.AIR && !shouldBeFrame) || (block != BCBuildersBlocks.frame && block != Blocks.AIR && shouldBeFrame)) {
                            currentTask = new TaskBreakBlock(pos);
                            return;
                        }
                    } else if(i == 1) {
                        if(shouldBeFrame && block == Blocks.AIR) {
                            currentTask = new TaskAddFrame(pos);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("box", box.writeToNBT());
        nbt.setTag("min", NBTUtils.writeBlockPos(min));
        nbt.setTag("max", NBTUtils.writeBlockPos(max));
        if(boxIterator != null) {
            nbt.setTag("box_iterator", boxIterator.writeToNBT());
        }
        nbt.setTag("mj_battery", battery.serializeNBT());
        if(currentTask != null) {
            nbt.setString("current_task_class", currentTask.getClass().getName());
            nbt.setTag("current_task_data", currentTask.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        box.initialize(nbt.getCompoundTag("box"));
        min = NBTUtils.readBlockPos(nbt.getTag("min"));
        max = NBTUtils.readBlockPos(nbt.getTag("max"));
        boxIterator = new BoxIterator().readFromNBT(nbt.getCompoundTag("box_iterator"));
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
        if(nbt.hasKey("current_task_class")) {
            try {
                currentTask = (Task) Class.forName(nbt.getString("current_task_class")).getDeclaredConstructor(TileQuarry.class).newInstance(this);
                if(nbt.hasKey("current_task_data")) {
                    currentTask.deserializeNBT(nbt.getCompoundTag("current_task_data"));
                }
            } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            currentTask = null;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("min = " + min);
        left.add("max = " + max);
        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return mjCapHelper.hasCapability(capability, facing) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(mjCapHelper.hasCapability(capability, facing)) {
            return mjCapHelper.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    private abstract class Task implements INBTSerializable<NBTTagCompound> {
        protected long energy = 0;

        protected abstract long getTarget();

        /**
         * @return true means that task is canceled
         */
        protected abstract boolean energyReceived();

        protected abstract void finish();

        public long getEnergy() {
            return energy;
        }

        /**
         * @return true means that task is canceled
         */
        public boolean addEnergy(long count) {
            if(count == 0) {
                return false;
            }
            energy += count;
            if(energy >= getTarget()) {
                finish();
                return true;
            } else {
                return energyReceived();
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setLong("energy", energy);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            energy = nbt.getLong("energy");
        }
    }

    private class TaskBreakBlock extends Task {
        BlockPos pos;

        TaskBreakBlock() {
        }

        public TaskBreakBlock(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        protected long getTarget() {
            return BlockUtils.computeBlockBreakPower(worldObj, pos);
        }

        @Override
        protected boolean energyReceived() {
            if(!worldObj.isAirBlock(pos)) {
                worldObj.sendBlockBreakProgress(pos.hashCode(), pos, (int) (energy * 9 / getTarget()));
                return false;
            } else {
                return true;
            }
        }

        @Override
        protected void finish() {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(worldObj, pos, worldObj.getBlockState(pos), FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) worldObj).get());
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if(!breakEvent.isCanceled()) {
                worldObj.sendBlockBreakProgress(pos.hashCode(), pos, -1);
                worldObj.destroyBlock(pos, false);
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            pos = NBTUtils.readBlockPos(nbt.getTag("pos"));
        }
    }

    private class TaskAddFrame extends Task {
        BlockPos pos;

        TaskAddFrame() {
        }

        public TaskAddFrame(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        protected long getTarget() {
            return 10000000;
        }

        @Override
        protected boolean energyReceived() {
            return !worldObj.isAirBlock(pos);
        }

        @Override
        protected void finish() {
            if(worldObj.isAirBlock(pos)) {
                for(int slot = 8; slot >= 0; slot--) {
                    ItemStack stackInSlot = invFrames.getStackInSlot(slot);
                    if(stackInSlot != null) {
                        worldObj.setBlockState(pos, BCBuildersBlocks.frame.getDefaultState());
                        invFrames.setStackInSlot(slot, stackInSlot.stackSize > 0 ? new ItemStack(stackInSlot.getItem(), stackInSlot.stackSize - 1) : null);
                        return;
                    }
                }
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setTag("pos", NBTUtils.writeBlockPos(pos));
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            pos = NBTUtils.readBlockPos(nbt.getCompoundTag("pos"));
        }
    }
}
