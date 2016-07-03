package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.mj.MjAPI;
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
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileMiningWell extends TileBC_Neptune implements ITickable, IHasWork, IControllable, IDebuggable {
    public static final int NET_LED_STATUS = 10;

    private int progress = 0;
    private BlockPos currentPos = null;
    // Used to check if this has completed all work
    private boolean isComplete = false;
    private Mode mode = Mode.On;
    private final MjBattery battery = new MjBattery(1000 * MjAPI.MJ);

    private void initCurrentPos() {
        if (currentPos == null) {
            currentPos = getPos();
            currentPos = currentPos.down();
        }
    }

    @Override
    public void update() {
        if (worldObj.isRemote || mode != Mode.On) {
            return;
        }

        // test with the output of a stone engine
        if (!battery.isFull()) {
            battery.addPower(MjAPI.MJ);// remove this
        }

        battery.tick(getWorld(), getPos());

        if (worldObj.rand.nextDouble() > 0.9) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        if (isComplete) {
            return;
        }

        initCurrentPos();

        IBlockState state = worldObj.getBlockState(currentPos);
        if (BlockUtils.isUnbreakableBlock(getWorld(), currentPos)) {
            setComplete(true);
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
                    setComplete(true);
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
                setComplete(true);
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

    private void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
        if (worldObj.isRemote) {
            sendNetworkUpdate(NET_LED_STATUS);
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                boolean[] flags = { isComplete, mode == Mode.On };
                MessageUtil.writeBooleanArray(buffer, flags);
                battery.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                boolean[] flags = MessageUtil.readBooleanArray(buffer, 2);
                isComplete = flags[0];
                mode = flags[1] ? Mode.On : Mode.Off;
                battery.readFromBuffer(buffer);
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

    // Rendering

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public float getPercentFilledForRender() {
        float val = battery.getContained() / (float) battery.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }
}
