package buildcraft.factory.tile;

import buildcraft.api.core.BCLog;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.lib.delta.DeltaInt;
import buildcraft.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

public abstract class TileMiner extends TileBC_Neptune implements ITickable, IHasWork, IDebuggable {
    public static final int NET_LED_STATUS = 10;

    protected int progress = 0;
    protected BlockPos currentPos = null;

    public final DeltaInt deltaTubeLength = deltaManager.addDelta("tubeY", EnumNetworkVisibility.RENDER);

    protected boolean isComplete = false;
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

    @Override
    public void update() {
        deltaManager.tick();

        if (world.isRemote) {
            return;
        }

         if (!battery.isFull()) {
//         test with the output of a stone engine
            battery.addPower(MjAPI.MJ);// remove this
         }

        battery.tick(getWorld(), getPos());

        // if (worldObj.rand.nextDouble() > 0.9) { // is this correct?
        if (true) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        initCurrentPos();

        mine();
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

    public boolean isComplete() {
        return world.isRemote ? isComplete : currentPos == null;
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
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (currentPos != null) {
            nbt.setTag("currentPos", NBTUtil.createPosTag(currentPos));
        }
        nbt.setInteger("progress", progress);
        nbt.setTag("mj_battery", battery.serializeNBT());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("currentPos")) {
            currentPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("currentPos"));
        }
        progress = nbt.getInteger("progress");
        battery.deserializeNBT(nbt.getCompoundTag("mj_battery"));
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                buffer.writeBoolean(isComplete());
                battery.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
            } else if (id == NET_LED_STATUS) {
                isComplete = buffer.readBoolean();
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
        left.add("isComplete = " + isComplete());
        left.add("progress = " + progress);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(new BlockPos(pos.getX(), 0, pos.getZ()), new BlockPos(pos.getX(),  world.getHeight(), pos.getZ()));
    }

    // IHasWork

    @Override
    public boolean hasWork() {
        return !isComplete;
    }

    // Capability

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T cap = mjCapHelper.getCapability(capability, facing);
        if (cap != null) {
            return cap;
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
