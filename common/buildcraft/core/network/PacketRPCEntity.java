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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PacketRPCEntity extends PacketRPC {
	private Entity entity;
	private int entityId;

	public PacketRPCEntity() {
	}

	public PacketRPCEntity(Entity iEntity, ByteBuf bytes) {
		entity = iEntity;
		contents = bytes;
	}

	@Override
	public void call(EntityPlayer sender) {
		super.call(sender);

		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		entity = sender.worldObj.getEntityByID(entityId);

		if (entity != null) {
			RPCHandler.receiveRPC(entity, info, contents);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		entityId = data.readInt();
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		data.writeInt(entity.getEntityId());
	}
}
