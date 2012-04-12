package net.minecraft.src.buildcraft.core.network;

import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class PacketTileUpdate extends PacketUpdate {

    public PacketTileUpdate() {
    	super(PacketIds.TILE_UPDATE);
    }

	public PacketTileUpdate(ISynchronizedTile tile) {
    	super(PacketIds.TILE_UPDATE);

		payload = tile.getPacketPayload();

		TileEntity entity = (TileEntity)tile;
		posX = entity.xCoord;
		posY = entity.yCoord;
		posZ = entity.zCoord;

		this.isChunkDataPacket = true;

	}

	public boolean targetExists(World world) {
		return world.blockExists(posX, posY, posZ);
	}

	public TileEntity getTarget(World world) {
		return world.getBlockTileEntity(posX, posY, posZ);
	}

}
