/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.api.core.ISerializable;

public class PacketTileUpdate extends PacketUpdate {

	public PacketTileUpdate() {
		super(PacketIds.TILE_UPDATE);
	}

	public PacketTileUpdate(ISerializable tile) {
		super(PacketIds.TILE_UPDATE);

		TileEntity entity = (TileEntity) tile;
		pos = entity.getPos();

		this.isChunkDataPacket = true;
		this.payload = tile;
	}

	public boolean targetExists(World world) {
		return world.isBlockLoaded(pos);
	}

	public TileEntity getTarget(World world) {
		return world.getTileEntity(pos);
	}

}
