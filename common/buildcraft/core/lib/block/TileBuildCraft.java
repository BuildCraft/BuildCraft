/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.ISerializable;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.TileBuffer;
import buildcraft.core.lib.network.PacketTileUpdate;
import buildcraft.core.lib.network.base.Packet;

import io.netty.buffer.ByteBuf;

/** For future maintainers: This class intentionally does not implement just every interface out there. For some of them
 * (such as IControllable), we expect the tiles supporting it to implement it - but TileBuildCraft provides all the
 * underlying functionality to stop code repetition. */
public abstract class TileBuildCraft extends TileEntity implements IEnergyHandler, ISerializable, IUpdatePlayerListBox, IAdditionalDataTile {
    protected TileBuffer[] cache;
    protected HashSet<EntityPlayer> guiWatchers = new HashSet<EntityPlayer>();
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
                    ledPower = (int) (3 * battery.getEnergyStored() / battery.getMaxEnergyStored());
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

    public void writeData(ByteBuf stream) {
        stream.writeByte(ledPower);
    }

    public void readData(ByteBuf stream) {
        ledPower = stream.readByte();
    }

    public Packet getPacketUpdate() {
        return new PacketTileUpdate(this, this);
    }

    @Override
    public net.minecraft.network.Packet getDescriptionPacket() {
        sendNetworkUpdate();
        return null;
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
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, pos, false);
        }
        return cache[side.ordinal()].getBlockState();
    }

    public TileEntity getTile(EnumFacing side) {
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, pos, false);
        }
        return cache[side.ordinal()].getTile();
    }

    public IControllable.Mode getControlMode() {
        return mode;
    }

    public void setControlMode(IControllable.Mode mode) {
        this.mode = mode;
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

    public String getCommandSenderName() {
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
