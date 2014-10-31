/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

public class PacketRPCTile extends PacketRPC {

	public TileEntity tile;

	int dimId;
	int x, y, z;

	public PacketRPCTile () {

	}

	public PacketRPCTile(TileEntity tile, ByteBuf bytes) {
		this.tile = tile;
		contents = bytes;
	}

	public void setTile (TileEntity aTile) {
		tile = aTile;
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		dimId = data.readShort();
		x = data.readInt();
		y = data.readShort();
		z = data.readInt();
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		// In order to save space on message, we assuming dimensions ids
		// small. Maybe worth using a varint instead
		data.writeShort(tile.getWorldObj().provider.dimensionId);
		data.writeInt(tile.xCoord);
		data.writeShort(tile.yCoord);
		data.writeInt(tile.zCoord);
	}

	@Override
	public void call (EntityPlayer sender) {
		World world = null;

		if (!sender.worldObj.isRemote) {
			// if this is a server, then get the world

			world = DimensionManager.getProvider(dimId).worldObj;
		} else if (sender.worldObj.provider.dimensionId == dimId) {
			// if the player is on this world, then synchronize things

			world = sender.worldObj;
		}

		TileEntity localTile = world.getTileEntity(x, y, z);

		setTile (localTile);

		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		RPCHandler.receiveRPC(localTile, info, contents);
	}
}
