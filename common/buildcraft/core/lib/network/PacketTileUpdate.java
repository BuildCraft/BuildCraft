/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.api.core.ISerializable;
import buildcraft.core.network.PacketIds;

public class PacketTileUpdate extends PacketUpdate {

	public PacketTileUpdate() {
		super(PacketIds.TILE_UPDATE);
	}

	public PacketTileUpdate(ISerializable tile) {
		super(PacketIds.TILE_UPDATE);

		TileEntity entity = (TileEntity) tile;
		posX = entity.xCoord;
		posY = entity.yCoord;
		posZ = entity.zCoord;

		this.isChunkDataPacket = true;
		this.payload = tile;
	}

	public boolean targetExists(World world) {
		return world.blockExists(posX, posY, posZ);
	}

	public TileEntity getTarget(World world) {
		return world.getTileEntity(posX, posY, posZ);
	}

}
