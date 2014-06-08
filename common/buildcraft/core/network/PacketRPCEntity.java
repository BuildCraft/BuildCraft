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
import io.netty.buffer.Unpooled;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PacketRPCEntity extends BuildCraftPacket {
	private byte[] contents;
	private Entity entity;
	private int entityId;

	public PacketRPCEntity() {
	}

	public PacketRPCEntity(Entity iEntity, byte[] bytes) {
		entity = iEntity;
		contents = bytes;
	}

	@Override
	public int getID() {
		return PacketIds.RPC_ENTITY;
	}

	public void call(EntityPlayer sender) {
		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		ByteBuf completeData = Unpooled.buffer();
		completeData.writeBytes(contents);

		entity = sender.worldObj.getEntityByID(entityId);

		if (entity != null) {
			RPCHandler.receiveRPC(entity, info, completeData);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		entityId = data.readInt();
		contents = new byte[data.readableBytes()];
		data.readBytes(contents);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeInt(entity.getEntityId());
		data.writeBytes(contents);
	}
}
