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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;

public abstract class PacketRPC extends BuildCraftPacket {

	public static HashMap<Integer, ByteBuf> bufferedPackets = new HashMap<Integer, ByteBuf>();
	public static int GLOBAL_ID = new Random(new Date().getTime()).nextInt();

	protected int id;

	protected ByteBuf contents;

	public PacketRPC() {
		id = GLOBAL_ID++;
	}

	@Override
	public final int getID() {
		return PacketIds.RPC;
	}

	public void call(EntityPlayer sender) {
		if (bufferedPackets.containsKey(id)) {
			ByteBuf data = bufferedPackets.remove(id);

			if (data != null) {
				contents = data.writeBytes(contents);
			}
		}
	}

	public ArrayList<PacketRPC> breakIntoSmallerPackets(int maxSize) {
		ArrayList<PacketRPC> messages = new ArrayList<PacketRPC>();

		if (contents.readableBytes() < maxSize) {
			messages.add(this);
			return messages;
		}

		int start = 0;

		while (true) {
			ByteBuf subContents = contents.readBytes(contents.readableBytes() > maxSize ? maxSize : contents
					.readableBytes());

			PacketRPCPart subPacket = new PacketRPCPart();
			subPacket.id = id;
			subPacket.contents = subContents;

			messages.add(subPacket);

			start += maxSize;

			if (contents.readableBytes() == 0) {
				break;
			}
		}

		contents = Unpooled.buffer();

		messages.add(this);

		return messages;
	}

	@Override
	public void readData(ByteBuf data) {
		id = data.readInt();
		int length = data.readInt();
		contents = Unpooled.buffer(length);
		data.readBytes(contents, length);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeInt(id);
		data.writeInt(contents.readableBytes());
		data.writeBytes(contents);
	}

}
