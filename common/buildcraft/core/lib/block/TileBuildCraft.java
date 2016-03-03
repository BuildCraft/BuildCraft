/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.ISerializable;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.TileBuffer;
import buildcraft.core.lib.network.PacketTileUpdate;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.NetworkUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** For future maintainers: This class intentionally does not implement just every interface out there. For some of them
 * (such as IControllable), we expect the tiles supporting it to implement it - but TileBuildCraft provides all the
 * underlying functionality to stop code repetition. */
public abstract class TileBuildCraft extends TileEntity implements IEnergyProvider, IEnergyReceiver, ISerializable, ITickable, IAdditionalDataTile {
    protected TileBuffer[] cache;
    protected HashSet<EntityPlayer> guiWatchers = new HashSet<>();
    protected IControllable.Mode mode;
    private boolean sendNetworkUpdate = false;

    protected int init = 0;
    private String owner = "[BuildCraft]";
    private RFBattery battery;

    private int receivedTick, extractedTick;
    private long worldTimeEnergyReceive;
    /** Used at the client for the power LED brightness */
    public int ledPower = 0, lastLedPower = 0;
    public boolean ledDone = false, lastLedDone = false;

    /** Used to help migrate existing worlds to whatever new blockstate format we use. Note that proper migration cannot
     * be implemented until this pre-release has gone out for a while now. */
    private NBTTagCompound lastBlockState = null;

    public String getOwner() {
        return owner;
    }

    public void addGuiWatcher(EntityPlayer player) {
        if (!guiWatchers.contains(player)) {
            guiWatchers.add(player);
        }
    }

    public void removeGuiWatcher(EntityPlayer player) {
        if (guiWatchers.contains(player)) {
            guiWatchers.remove(player);
        }
    }

    @Override
    public void update() {
        if (worldObj == null) throw new NullPointerException("worldObj");
        if (init != 2 && !isInvalid()) {
            if (init < 1) {
                init++;
                return;
            }
            initialize();
            init = 2;
        }

        if (battery != null) {
            receivedTick = 0;
            extractedTick = 0;

            if (!worldObj.isRemote) {
                int prePower = ledPower;
                int stored = battery.getEnergyStored();
                int max = battery.getMaxEnergyStored();
                ledPower = 0;
                if (stored != 0) {
                    ledPower = stored * 2 / max + 1;
                }
                if (prePower != ledPower) {
                    sendNetworkUpdate();
                }
            }
        }

        if (!worldObj.isRemote) {
            if (battery != null) {
                if (battery.getMaxEnergyStored() > 0) {
                    ledPower = 3 * battery.getEnergyStored() / battery.getMaxEnergyStored();
                } else {
                    ledPower = 0;
                }
            }
        }

        if (lastLedPower != ledPower || lastLedDone != ledDone) {
            if (worldObj.isRemote) {
                worldObj.markBlockForUpdate(getPos());
            } else {
                sendNetworkUpdate();
            }
            lastLedPower = ledPower;
            lastLedDone = ledDone;
        }

        if (sendNetworkUpdate) {
            if (worldObj != null && !worldObj.isRemote) {
                BuildCraftCore.instance.sendToPlayers(getPacketUpdate(), worldObj, getPos(), DefaultProps.NETWORK_UPDATE_RANGE);
                sendNetworkUpdate = false;
            }
        }
    }

    public void initialize() {

    }

    @Override
    public void validate() {
        super.validate();
        cache = null;
    }

    @Override
    public void invalidate() {
        init = 0;
        super.invalidate();
        cache = null;
    }

    public void onBlockPlacedBy(EntityLivingBase entity, ItemStack stack) {
        if (entity instanceof EntityPlayer) {
            owner = ((EntityPlayer) entity).getDisplayNameString();
        }
    }

    public void destroy() {
        cache = null;
    }

    @Override
    public void sendNetworkUpdate() {
        sendNetworkUpdate = true;
    }

    @Override
    public void writeData(ByteBuf stream) {
        stream.writeByte(ledPower);
        NetworkUtils.writeEnum(stream, mode);
    }

    @Override
    public void readData(ByteBuf stream) {
        ledPower = stream.readByte();
        mode = NetworkUtils.readEnum(stream, Mode.class);
    }

    public PacketTileUpdate getPacketUpdate() {
        return new PacketTileUpdate(this, this);
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("net-type", "desc-packet");
        Packet p = getPacketUpdate();
        ByteBuf buf = Unpooled.buffer();
        p.writeData(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        nbt.setByteArray("net-data", bytes);
        S35PacketUpdateTileEntity tileUpdate = new S35PacketUpdateTileEntity(getPos(), 0, nbt);
        return tileUpdate;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        if (!worldObj.isRemote) return;
        if (pkt.getNbtCompound() == null) throw new RuntimeException("No NBTTag compound! This is a bug!");
        NBTTagCompound nbt = pkt.getNbtCompound();
        try {
            if ("desc-packet".equals(nbt.getString("net-type"))) {
                byte[] bytes = nbt.getByteArray("net-data");
                ByteBuf data = Unpooled.wrappedBuffer(bytes);
                PacketTileUpdate p = new PacketTileUpdate();
                p.readData(data);
                // The player is not used so its fine
                p.applyData(worldObj, null);
            } else {
                BCLog.logger.warn("Recieved a packet with a different type that expected (" + nbt.getString("net-type") + ")");
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read a packet! (net-type=\"" + nbt.getTag("net-type") + "\", net-data=\"" + nbt.getTag("net-data")
                + "\")", t);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("owner", owner);
        if (battery != null) {
            NBTTagCompound batteryNBT = new NBTTagCompound();
            battery.writeToNBT(batteryNBT);
            nbt.setTag("battery", batteryNBT);
        }
        if (mode != null) {
            nbt.setByte("lastMode", (byte) mode.ordinal());
        }

        // Version tag that can be used for upgrading.
        // 0 means[1.8.9] 7.2.0-pre12 or before (default value)
        // 1 means [1.8.9] 7.2.0-pre13 up until 7.2.0-preX
        // 2 means [1.8.9] 7.2.0-preX or later
        nbt.setInteger("data-version", 1);

        /* Also save the state of all BC tiles. This will be helpful for migration. */
        // REMOVE THIS AFTER preX
        if (hasWorldObj()) {
            IBlockState blockstate = worldObj.getBlockState(getPos());
            Block block = blockstate.getBlock();
            if (block instanceof BlockBuildCraft) {
                // Assume that this is us- it would be odd for this tile to be with the wrong block.
                BlockBuildCraft bcBlock = (BlockBuildCraft) block;
                NBTTagCompound statenbt = new NBTTagCompound();
                for (IProperty<?> prop : bcBlock.properties) {
                    Object value = blockstate.getValue(prop);
                    if (value == null) continue;
                    statenbt.setTag(prop.getName(), NBTUtils.writeObject(value));
                }
                nbt.setTag("blockstate", statenbt);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("owner")) {
            owner = nbt.getString("owner");
        }
        if (battery != null) {
            battery.readFromNBT(nbt.getCompoundTag("battery"));
        }
        if (nbt.hasKey("lastMode")) {
            mode = IControllable.Mode.values()[nbt.getByte("lastMode")];
        }

        int version = nbt.getInteger("data-version");

        // Load up the block from pre12 -> preX
        if (nbt.hasKey("blockstate") && version == 1) lastBlockState = nbt.getCompoundTag("blockstate");
    }

    protected int getTicksSinceEnergyReceived() {
        return (int) (worldObj.getTotalWorldTime() - worldTimeEnergyReceive);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object cmp) {
        return this == cmp;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return battery != null;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if (battery != null && this.canConnectEnergy(from)) {
            int received = battery.receiveEnergy(maxReceive - receivedTick, simulate);
            if (!simulate) {
                receivedTick += received;
                worldTimeEnergyReceive = worldObj.getTotalWorldTime();
            }
            return received;
        } else {
            return 0;
        }
    }

    /** If you want to use this, implement IEnergyProvider. */
    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        if (battery != null && this.canConnectEnergy(from)) {
            int extracted = battery.extractEnergy(maxExtract - extractedTick, simulate);
            if (!simulate) {
                extractedTick += extracted;
            }
            return extracted;
        } else {
            return 0;
        }
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        if (battery != null && this.canConnectEnergy(from)) {
            return battery.getEnergyStored();
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        if (battery != null && this.canConnectEnergy(from)) {
            return battery.getMaxEnergyStored();
        } else {
            return 0;
        }
    }

    public RFBattery getBattery() {
        return battery;
    }

    protected void setBattery(RFBattery battery) {
        this.battery = battery;
    }

    public IBlockState getBlockState(EnumFacing side) {
        throwNullWorldCrash();
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, pos, false);
        }
        return cache[side.ordinal()].getBlockState();
    }

    public TileEntity getTile(EnumFacing side) {
        throwNullWorldCrash();
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, pos, false);
        }
        return cache[side.ordinal()].getTile();
    }

    private void throwNullWorldCrash() {
        if (worldObj != null) return;
        CrashReport crash = new CrashReport("Attempted to create a cache for a BC tile without a world! WTF? Thats a bad idea!",
                new NullPointerException("worldObj"));
        CrashReportCategory cat = crash.makeCategory("BC tile debug info");
        cat.addCrashSection("VN::getPos()", getPos());
        cat.addCrashSection("VN::isInvalid()", this.isInvalid());
        cat.addCrashSection("BC::init", init);
        throw new ReportedException(crash);
    }

    public IControllable.Mode getControlMode() {
        return mode;
    }

    public void setControlMode(IControllable.Mode mode) {
        this.mode = mode;
        sendNetworkUpdate();
    }

    // IInventory

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {}

    public int getFieldCount() {
        return 0;
    }

    public String getInventoryName() {
        return "";
    }

    public String getName() {
        return getInventoryName();
    }

    public IChatComponent getDisplayName() {
        return new ChatComponentText(getInventoryName());
    }

    public void clear() {}

    public boolean hasCustomName() {
        return !StringUtils.isEmpty(getInventoryName());
    }
}
