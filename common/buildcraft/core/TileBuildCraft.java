/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.HashSet;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IWorldNameable;
import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.ISerializable;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.ISynchronizedTile;
import buildcraft.core.network.PacketTileUpdate;
import buildcraft.core.utils.Utils;

public abstract class TileBuildCraft extends TileEntity implements IEnergyHandler, ISynchronizedTile, ISerializable, IUpdatePlayerListBox, IWorldNameable {
    protected TileBuffer[] cache;
	protected HashSet<EntityPlayer> guiWatchers = new HashSet<EntityPlayer>();

	private boolean init = false;
	private String owner = "[BuildCraft]";
	private RFBattery battery;

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
		if (!init && !isInvalid()) {
			initialize();
			init = true;
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
		init = false;
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

	public void sendNetworkUpdate() {
		if (worldObj != null && !worldObj.isRemote) {
			BuildCraftCore.instance.sendToPlayers(getPacketUpdate(), worldObj,
					pos.getX(), pos.getY(), pos.getZ(), DefaultProps.NETWORK_UPDATE_RANGE);
		}
	}

	public void writeData(ByteBuf stream) {

	}

	public void readData(ByteBuf stream) {

	}

	public BuildCraftPacket getPacketUpdate() {
		return new PacketTileUpdate(this);
	}

	@Override
	public Packet getDescriptionPacket() {
		return Utils.toPacket(getPacketUpdate(), 0);
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
	public int receiveEnergy(EnumFacing from, int maxReceive,
			boolean simulate) {
		if (battery != null && this.canConnectEnergy(from)) {
			return battery.receiveEnergy(maxReceive, simulate);
		} else {
			return 0;
		}
	}

	/**
	 * If you want to use this, implement IEnergyProvider.
	 */
	@Override
	public int extractEnergy(EnumFacing from, int maxExtract,
			boolean simulate) {
		if (battery != null && this.canConnectEnergy(from)) {
			return battery.extractEnergy(maxExtract, simulate);
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

    public Block getBlock(EnumFacing side) {
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, pos, false);
        }
        return cache[side.ordinal()].getBlock();
    }

    public TileEntity getTile(EnumFacing side) {
        if (cache == null) {
            cache = TileBuffer.makeBuffer(worldObj, pos, false);
        }
        return cache[side.ordinal()].getTile();
    }

	// Helpers for overriding

    @Override
	public boolean hasCustomName() {
		return false;
	}
	
	public String getName() {
		return "";
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentTranslation(getName());
	}
	
	@Override
	public String getCommandSenderName() {
		return "";
	}

	public int getField(int id) {
		return 0;
	}

	public void setField(int id, int value) {

	}

	public int getFieldCount() {
		return 0;
	}

	public void clear() {

	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(pos) == this && entityplayer.getDistanceSq(pos) <= 64.0D;
	}
}
