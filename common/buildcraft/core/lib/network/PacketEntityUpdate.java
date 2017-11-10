/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import buildcraft.api.core.ISerializable;
import buildcraft.core.network.PacketIds;

public class PacketEntityUpdate extends PacketUpdate {
	public int entityId;

	public PacketEntityUpdate() {
		super(PacketIds.ENTITY_UPDATE);
	}

	public PacketEntityUpdate(ISerializable payload) {
		this(PacketIds.ENTITY_UPDATE, payload);
	}

	public PacketEntityUpdate(int packetId, ISerializable payload) {
		super(packetId, payload);

		Entity entity = (Entity) payload;
		entityId = entity.getEntityId();
	}

	@Override
	public void writeIdentificationData(ByteBuf data) {
		data.writeInt(entityId);
	}

	@Override
	public void readIdentificationData(ByteBuf data) {
		entityId = data.readInt();
	}

	public boolean targetExists(World world) {
		return world.getEntityByID(entityId) != null;
	}

	public Entity getTarget(World world) {
		return world.getEntityByID(entityId);
	}
}
