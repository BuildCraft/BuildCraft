package buildcraft.factory.tile;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.factory.BCFactoryBlocks;
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
    public static final int NET_WANTED_Y = 11;

    protected int progress = 0;
    protected BlockPos currentPos = null;

    private int wantedY = -1;
    private double currentY = 0;
    private double lastY = 0;

    protected boolean isComplete = false;
    protected final MjBattery battery = new MjBattery(500 * MjAPI.MJ);
    protected final IMjReceiver mjReceiver = createMjReceiver();
    protected final MjCapabilityHelper mjCapHelper = new MjCapabilityHelper(mjReceiver);

    protected abstract void initCurrentPos();

    protected abstract void mine();

    protected abstract IMjReceiver createMjReceiver();

    @Override
    public void update() {
        if (world.isRemote) {
            lastY = currentY;
            if (Math.abs(wantedY - currentY) <= 0.1) {
                currentY = wantedY;
            } else {
                currentY = currentY + Math.min(Math.abs((wantedY - currentY) / 10D), 0.1) * (wantedY > currentY ? 1 : -1);
            }
            return;
        }

        battery.tick(getWorld(), getPos());

        // if (worldObj.rand.nextDouble() > 0.9) { // is this correct?
        if (true) {
            sendNetworkUpdate(NET_LED_STATUS);
        }

        initCurrentPos();

        mine();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (int y = pos.getY() - 1; y > 0; y--) {
            BlockPos blockPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (world.getBlockState(blockPos).getBlock() == BCFactoryBlocks.tube) {
                world.setBlockToAir(blockPos);
            } else {
                break;
            }
        }
    }

    protected void goToYLevel(int newY) {
        if (newY != wantedY) {
            for (int y = pos.getY() - 1; y > 0; y--) {
                BlockPos blockPos = new BlockPos(pos.getX(), y, pos.getZ());
                if (world.getBlockState(blockPos).getBlock() == BCFactoryBlocks.tube) {
                    world.setBlockToAir(blockPos);
                } else {
                    break;
                }
            }
            for (int y = pos.getY() - 1; y > newY; y--) {
                BlockPos blockPos = new BlockPos(pos.getX(), y, pos.getZ());
                world.setBlockState(blockPos, BCFactoryBlocks.tube.getDefaultState());
            }
            currentY = wantedY = newY;
            sendNetworkUpdate(NET_WANTED_Y);
        }
    }

    public double getY(float partialTicks) {
        if (partialTicks <= 0) {
            return lastY;
        } else if (partialTicks >= 1) {
            return currentY;
        } else {
            return lastY * (1 - partialTicks) + currentY * partialTicks;
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
            battery.addPower(energy * 100, false);
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
                buffer.writeDouble(currentY);
            } else if (id == NET_LED_STATUS) {
                buffer.writeBoolean(isComplete());
                battery.writeToBuffer(buffer);
            } else if (id == NET_WANTED_Y) {
                buffer.writeInt(wantedY);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
                currentY = buffer.readDouble();
            } else if (id == NET_LED_STATUS) {
                isComplete = buffer.readBoolean();
                battery.readFromBuffer(buffer);
            } else if (id == NET_WANTED_Y) {
                boolean firstTime = wantedY == -1;
                wantedY = buffer.readInt();
                if (firstTime) {
                    currentY = lastY = pos.down().getY();
                }
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("battery = " + battery.getDebugString());
        left.add("current = " + currentPos);
        left.add("wantedY = " + wantedY);
        left.add("currentY = " + currentY);
        left.add("lastY = " + lastY);
        left.add("isComplete = " + isComplete());
        left.add("progress = " + progress);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
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
