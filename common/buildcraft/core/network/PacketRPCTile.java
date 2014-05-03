/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;

public class PacketRPCTile extends BuildCraftPacket {
	public static int GLOBAL_ID = new Random(new Date().getTime()).nextInt();
	public static HashMap<Integer, ByteBuf> bufferedPackets = new HashMap<Integer, ByteBuf>();
	public TileEntity tile;

	byte [] contents;
	int id;
	boolean moreDataToCome = false;

	int dimId;
	int x, y, z;

	public PacketRPCTile () {
		id = GLOBAL_ID++;
	}

	public PacketRPCTile (TileEntity tile, byte [] bytes) {
		this.tile = tile;
		contents = bytes;
	}

	public void setTile (TileEntity aTile) {
		tile = aTile;
	}

	@Override
	public int getID() {
		return PacketIds.RPC_TILE;
	}

	@Override
	public void readData(ByteBuf data) {
		dimId = data.readShort();

		x = data.readInt();
		y = data.readInt();
		z = data.readInt();
		id = data.readInt ();
		moreDataToCome = data.readBoolean();
		contents = new byte [data.readableBytes()];
		data.readBytes(contents);
	}

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

		ByteBuf previousData = bufferedPackets.get(id);
		bufferedPackets.remove(id);

		ByteBuf completeData;

		if (previousData != null) {
			completeData = previousData.writeBytes(contents);
		} else {
			completeData = Unpooled.buffer();
			completeData.writeBytes(contents);
		}

		if (!moreDataToCome) {
			RPCHandler.receiveRPC(localTile, info, completeData);
		} else {
			bufferedPackets.put(id, completeData);
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		// In order to save space on message, we assuming dimensions ids
		// small. Maybe worth using a varint instead
		data.writeShort(tile.getWorldObj().provider.dimensionId);
		data.writeInt(tile.xCoord);
		data.writeInt(tile.yCoord);
		data.writeInt(tile.zCoord);

		data.writeInt(id);
		data.writeBoolean(moreDataToCome);
		data.writeBytes(contents);
	}

	public ArrayList<PacketRPCTile> breakIntoSmallerPackets(int maxSize) {
		ArrayList<PacketRPCTile> messages = new ArrayList<PacketRPCTile>();

		if (contents.length < maxSize) {
			messages.add(this);
			return messages;
		}

		int start = 0;

		while (true) {
			byte [] subContents = ArrayUtils.subarray(contents, start, start + maxSize);

			PacketRPCTile subPacket = new PacketRPCTile();
			subPacket.id = id;
			subPacket.contents = subContents;
			subPacket.tile = tile;

			messages.add(subPacket);

			start += maxSize;

			if (start >= contents.length) {
				subPacket.moreDataToCome = false;
				break;
			} else {
				subPacket.moreDataToCome = true;
			}
		}

		return messages;
	}

}
