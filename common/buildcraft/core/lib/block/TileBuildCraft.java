/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.block;

import java.util.HashSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.ISerializable;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.TileBuffer;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.PacketTileUpdate;
import buildcraft.core.lib.utils.Utils;

/**
 * For future maintainers: This class intentionally does not implement
 * just every interface out there. For some of them (such as IControllable),
 * we expect the tiles supporting it to implement it - but TileBuildCraft
 * provides all the underlying functionality to stop code repetition.
 */
public abstract class TileBuildCraft extends TileEntity implements IEnergyHandler, ISerializable {
	protected TileBuffer[] cache;
	protected HashSet<EntityPlayer> guiWatchers = new HashSet<EntityPlayer>();
	protected IControllable.Mode mode;

	private boolean init = false;
	private String owner = "[BuildCraft]";
	private RFBattery battery;

	private int receivedTick, extractedTick;
	private long worldTimeEnergyReceive;

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
	public void updateEntity() {
		if (!init && !isInvalid()) {
			initialize();
			init = true;
		}

		if (battery != null) {
			receivedTick = 0;
			extractedTick = 0;
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
			owner = ((EntityPlayer) entity).getDisplayName();
		}
	}

	public void destroy() {
		cache = null;
	}

	public void sendNetworkUpdate() {
		if (worldObj != null && !worldObj.isRemote) {
			BuildCraftCore.instance.sendToPlayers(getPacketUpdate(), worldObj,
					xCoord, yCoord, zCoord, getNetworkUpdateRange());
		}
	}

	protected int getNetworkUpdateRange() {
		return DefaultProps.NETWORK_UPDATE_RANGE;
	}

	public void writeData(ByteBuf stream) {

	}

	public void readData(ByteBuf stream) {

	}

	public Packet getPacketUpdate() {
		return new PacketTileUpdate(this);
	}

	@Override
	public net.minecraft.network.Packet getDescriptionPacket() {
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
		return (xCoord * 37 + yCoord) * 37 + zCoord;
	}

	@Override
	public boolean equals(Object cmp) {
		return this == cmp;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return battery != null;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
							 boolean simulate) {
		if (battery != null && this.canConnectEnergy(from)) {
			int received = battery.receiveEnergy(Math.min(maxReceive, battery.getMaxEnergyReceive() - receivedTick), simulate);
			if (!simulate) {
				receivedTick += received;
				worldTimeEnergyReceive = worldObj.getTotalWorldTime();
			}
			return received;
		} else {
			return 0;
		}
	}

	/**
	 * If you want to use this, implement IEnergyProvider.
	 */
	public int extractEnergy(ForgeDirection from, int maxExtract,
							 boolean simulate) {
		if (battery != null && this.canConnectEnergy(from)) {
			int extracted = battery.extractEnergy(Math.min(maxExtract, battery.getMaxEnergyExtract() - extractedTick), simulate);
			if (!simulate) {
				extractedTick += extracted;
			}
			return extracted;
		} else {
			return 0;
		}
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if (battery != null && this.canConnectEnergy(from)) {
			return battery.getEnergyStored();
		} else {
			return 0;
		}
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
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

	public Block getBlock(ForgeDirection side) {
		if (cache == null) {
			cache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}
		return cache[side.ordinal()].getBlock();
	}

	public TileEntity getTile(ForgeDirection side) {
		if (cache == null) {
			cache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}
		return cache[side.ordinal()].getTile();
	}

	public IControllable.Mode getControlMode() {
		return mode;
	}

	public void setControlMode(IControllable.Mode mode) {
		this.mode = mode;
	}
}
