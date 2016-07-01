package buildcraft.factory.tile;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileMiningWell extends TileBC_Neptune implements ITickable, IHasWork, IControllable, IDebuggable, ITileLed {
    private int progress = 0;
    private BlockPos currentPos = null;
    // Used to check if this has completed all work
    private boolean isComplete = false;
    private Mode mode = Mode.On;
    private final MjBattery battery = new MjBattery(1000_000);

    private void initCurrentPos() {
        if (currentPos == null) {
            currentPos = getPos();
            currentPos = currentPos.down();
        }
    }

    @Override
    public void update() {
        if (worldObj.isRemote || isComplete || mode != Mode.On) {
            return;
        }
        // test with the output of a stone engine
        battery.addPower(1000);// remove this

        initCurrentPos();

        IBlockState state = worldObj.getBlockState(currentPos);
        if (BlockUtils.isUnbreakableBlock(getWorld(), currentPos)) {
            isComplete = true;
            return;
        }

        int target = BlockUtils.computeBlockBreakPower(worldObj, currentPos);
        progress += battery.extractPower(0, target - progress);

        if (progress >= target) {
            progress = 0;
            if (!worldObj.isAirBlock(currentPos)) {
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(worldObj, currentPos, state, FakePlayerUtil.INSTANCE.getBuildCraftPlayer((WorldServer) worldObj).get());
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (breakEvent.isCanceled()) {
                    isComplete = true;
                    return;
                }
                List<ItemStack> stacks = BlockUtils.getItemStackFromBlock((WorldServer) worldObj, currentPos, pos);
                if (stacks != null) {
                    for (ItemStack stack : stacks) {
                        stack.stackSize -= Utils.addToRandomInventoryAround(worldObj, pos, stack);
                        if (stack.stackSize > 0) {
                            stack.stackSize -= Utils.addToRandomInjectableAround(worldObj, pos, null, stack);
                        }
                        InvUtils.dropItemUp(getWorld(), stack, getPos());
                    }
                }
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                worldObj.destroyBlock(currentPos, false);
            }
            worldObj.setBlockState(currentPos, BCFactoryBlocks.plainPipe.getDefaultState());
            worldObj.scheduleUpdate(currentPos, BCFactoryBlocks.plainPipe, 100);
            currentPos = currentPos.down();
            if (currentPos.getY() < 0) {
                isComplete = true;
            }
        } else {
            if (!worldObj.isAirBlock(currentPos)) {
                worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (progress * 9) / target);
            }
        }
    }

    @Override
    protected void migrateOldNBT(int version, NBTTagCompound nbt) {
        if (version == BCVersion.BEFORE_RECORDS.dataVersion || version == BCVersion.v7_2_0_pre_12.dataVersion) {
            NBTTagCompound oldBattery = nbt.getCompoundTag("battery");
            int energy = oldBattery.getInteger("energy");
            battery.extractPower(0, Integer.MAX_VALUE);
            battery.addPower(energy * 100);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        currentPos = new BlockPos(nbt.getInteger("currentX"), nbt.getInteger("currentY"), nbt.getInteger("currentZ"));
        progress = nbt.getInteger("progress");
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        initCurrentPos();
        nbt.setInteger("currentX", currentPos.getX());
        nbt.setInteger("currentY", currentPos.getY());
        nbt.setInteger("currentZ", currentPos.getZ());
        nbt.setInteger("progress", progress);
        nbt.setTag("mj_battery", battery.serializeNBT());
        return nbt;
    }

    @Override
    public void onRemove() {
        worldObj.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
        for (int y = currentPos.getY(); y < pos.getY(); y++) {
            BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
            if (worldObj.getBlockState(p).getBlock() == BCFactoryBlocks.plainPipe) {
                worldObj.destroyBlock(p, false);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("current = " + currentPos);
        left.add("isComplete = " + isComplete);
        left.add("mode = " + mode);
        left.add("progress  = " + progress);
    }

    // IHasWork

    @Override
    public boolean hasWork() {
        return !isComplete;
    }

    // IControllable

    @Override
    public Mode getControlMode() {
        return mode;
    }

    @Override
    public void setControlMode(Mode mode) {
        if (acceptsControlMode(mode)) {
            this.mode = mode;
        }
    }

    @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == Mode.Off || mode == Mode.On;
    }

    // ITileLed

    @Override
    public boolean isDone() {
        return isComplete;
    }

    @Override
    public int getPowerLevel() {
        return (int) ((float)battery.getContained() / battery.getCapacity() * 4);
    }

    @Override
    public int getX() {
        return 12;
    }

    @Override
    public int getY() {
        return 5;
    }
}
