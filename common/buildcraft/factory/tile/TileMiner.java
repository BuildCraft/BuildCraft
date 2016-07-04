package buildcraft.factory.tile;

import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

public abstract class TileMiner  extends TileBC_Neptune implements ITickable, IHasWork, IControllable, IDebuggable {
    public static final int NET_LED_STATUS = 10;

    protected int progress = 0;
    protected BlockPos currentPos = null;
    public double tubeY = 0; // TODO: replace with delta
    // Used to check if this has completed all work
    protected boolean isComplete = false;
    protected Mode mode = Mode.On;
    protected final MjBattery battery = new MjBattery(1000_000);

    protected void initCurrentPos() {
        if (currentPos == null) {
            currentPos = getPos();
            currentPos = currentPos.down();
        }
    }

    protected abstract void mine();

    protected int getTubeOffset() {
        return 0;
    }

    @Override
    public void update() {
        if (worldObj.isRemote || mode != Mode.On) {
            return;
        }
        battery.tick(getWorld(), getPos());

        // test with the output of a stone engine
        battery.addPower(1000);// remove this

        // if (worldObj.rand.nextDouble() > 0.9) { // is this correct?
        if(true) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        if(tubeY == 0) {
            tubeY = pos.getY();
        } else {
            double diff = currentPos.getY() - tubeY + this.getTubeOffset();
            if(Math.abs(diff) <= 0.01) {
                tubeY = currentPos.getY() + this.getTubeOffset();
            } else if(diff > 0) {
                tubeY += Math.max(diff * 0.05, 0.01);
            } else {
                tubeY += Math.min(diff * 0.05, -0.01);
            }
        }

        if (isComplete) {
            return;
        }

        initCurrentPos();

        mine();
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
        tubeY = nbt.getDouble("tubeY");
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
        nbt.setDouble("tubeY", tubeY);
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
                buffer.writeDouble(tubeY);
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
                tubeY = buffer.readDouble();
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("current = " + currentPos);
        left.add("tube = " + tubeY);
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
