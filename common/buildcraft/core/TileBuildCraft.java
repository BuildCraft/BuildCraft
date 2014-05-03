/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.BuildCraftCore;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.ISynchronizedTile;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketTileUpdate;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TilePacketWrapper;
import buildcraft.core.utils.Utils;

public abstract class TileBuildCraft extends TileEntity implements ISynchronizedTile {

	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> updateWrappers = new HashMap<Class, TilePacketWrapper>();
	@SuppressWarnings("rawtypes")
	private static Map<Class, TilePacketWrapper> descriptionWrappers = new HashMap<Class, TilePacketWrapper>();
	private final TilePacketWrapper descriptionPacket;
	private final TilePacketWrapper updatePacket;
	private boolean init = false;
	private String owner = "[BuildCraft]";

	public TileBuildCraft() {
		if (!updateWrappers.containsKey(this.getClass())) {
			updateWrappers.put(this.getClass(), new TilePacketWrapper(this.getClass()));
		}

		if (!descriptionWrappers.containsKey(this.getClass())) {
			descriptionWrappers.put(this.getClass(), new TilePacketWrapper(this.getClass()));
		}

		updatePacket = updateWrappers.get(this.getClass());
		descriptionPacket = descriptionWrappers.get(this.getClass());

	}

	public String getOwner() {
		return owner;
	}

	@Override
	public void updateEntity() {
		if (!init && !isInvalid()) {
			initialize();
			init = true;
		}

		if (this instanceof IPowerReceptor) {
			IPowerReceptor receptor = (IPowerReceptor) this;
			receptor.getPowerReceiver(null).update();
		}
	}

	@Override
	public void invalidate() {
		init = false;
		super.invalidate();
	}

	public void initialize() {
		Utils.handleBufferedDescription(this);
	}

	public void onBlockPlacedBy(EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityPlayer) {
			owner = ((EntityPlayer) entity).getDisplayName();
		}
	}

	public void destroy() {
	}

	public void sendNetworkUpdate() {
		if (!worldObj.isRemote) {
			BuildCraftCore.instance.sendToPlayers(getUpdatePacket(), worldObj,
					xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE);
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		return Utils.toPacket(getUpdatePacket(), 0);
	}

	@Override
	public PacketPayload getPacketPayload() {
		return updatePacket.toPayload(this);
	}

	@Override
	public BuildCraftPacket getUpdatePacket() {
		return new PacketTileUpdate(this);
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		descriptionPacket.fromPayload(this, packet.payload);
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		updatePacket.fromPayload(this, packet.payload);
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("owner", owner);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey("owner")) {
			owner = nbt.getString("owner");
		}
	}

	public World getWorld() {
		return worldObj;
	}
}
