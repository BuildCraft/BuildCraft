package buildcraft.factory.tile;

import java.io.IOException;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class TileMiner extends TileBC_Neptune implements ITickable, IHasWork, IControllable, IDebuggable {
    public static final int NET_LED_STATUS = 10;

    protected int progress = 0;
    protected BlockPos currentPos = null;

    public final DeltaInt deltaTubeLength = deltaManager.addDelta("tubeY", EnumNetworkVisibility.RENDER);

    protected boolean isComplete = false;
    protected Mode mode = Mode.On;
    protected final MjBattery battery = new MjBattery(MjAPI.MJ * 500);
    protected final IMjReceiver mjReceiver = createMjReceiver();
    protected final MjCapabilityHelper mjCapHelper = new MjCapabilityHelper(mjReceiver);

    protected void initCurrentPos() {
        if (currentPos == null) {
            currentPos = getPos();
            currentPos = currentPos.down();
            goToYLevel(currentPos.getY());
        }
    }

    protected abstract void mine();

    protected abstract IMjReceiver createMjReceiver();

    public double getTubeOffset() {
        return 0;
    }

    @Override
    public void update() {
        deltaManager.tick();

        if (worldObj.isRemote) {
            return;
        }

        // if (!battery.isFull()) {
        // test with the output of a stone engine
        // battery.addPower(MjAPI.MJ);// remove this
        // }

        battery.tick(getWorld(), getPos());

        if (mode != Mode.On) {
            return;
        }

        // if (worldObj.rand.nextDouble() > 0.9) { // is this correct?
        if (true) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        if (isComplete) {
            return;
        }

        initCurrentPos();

        if (hasTubeStopped()) {
            mine();
        }
    }

    protected int getCurrentYLevel() {
        return getPos().getY() - 1 - deltaTubeLength.getStatic(false);
    }

    protected boolean isAtYlevel(int wanted) {
        return wanted == getCurrentYLevel();
    }

    protected boolean hasTubeStopped() {
        return deltaTubeLength.changingEntries.isEmpty();
    }

    protected void goToYLevel(int wanted) {
        int length = getPos().getY() - wanted - 1;
        int current = deltaTubeLength.getStatic(true);
        int diff = length - current;
        if (diff != 0) {
            deltaTubeLength.addDelta(0, 50 * diff, diff);
            BCLog.logger.info("Adding a delta " + diff);
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

    protected void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
        if (!worldObj.isRemote) {
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
        left.add("tube = " + deltaTubeLength.getStatic(false));
        left.add("isComplete = " + isComplete);
        left.add("mode = " + mode);
        left.add("progress = " + progress);
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

    // Capability

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (mjCapHelper.hasCapability(capability, facing)) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (mjCapHelper.hasCapability(capability, facing)) {
            return mjCapHelper.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    // Rendering

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public float getPercentFilledForRender() {
        float val = battery.getStored() / (float) battery.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }
}
