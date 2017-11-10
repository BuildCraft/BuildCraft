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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.api.core.ISerializable;
import buildcraft.core.network.PacketIds;

public class PacketTileUpdate extends PacketUpdate {
	public int posX;
	public int posY;
	public int posZ;

	public PacketTileUpdate() {
		super(PacketIds.TILE_UPDATE);
	}

	public PacketTileUpdate(ISerializable tile) {
		this(PacketIds.TILE_UPDATE, tile);
	}

	public PacketTileUpdate(int packetId, ISerializable tile) {
		super(packetId, tile);

		TileEntity entity = (TileEntity) tile;
		posX = entity.xCoord;
		posY = entity.yCoord;
		posZ = entity.zCoord;
	}

	@Override
	public void writeIdentificationData(ByteBuf data) {
		data.writeInt(posX);
		data.writeShort(posY);
		data.writeInt(posZ);
	}

	@Override
	public void readIdentificationData(ByteBuf data) {
		posX = data.readInt();
		posY = data.readShort();
		posZ = data.readInt();
	}

	public boolean targetExists(World world) {
		return world.blockExists(posX, posY, posZ);
	}

	public TileEntity getTarget(World world) {
		return world.getTileEntity(posX, posY, posZ);
	}

}
