package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftCore;

public class TilePacketWrapper {
	
	ClassMapping rootMapping;

	PacketIds packetType;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TilePacketWrapper (Class c, PacketIds packetType) {
		rootMapping = new ClassMapping(c, packetType);
		this.packetType = packetType;
	}
	
	public Packet230ModLoader toPacket (TileEntity tile) {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = mod_BuildCraftCore.instance.getId();
		packet.isChunkDataPacket = true;
		packet.packetType = packetType.ordinal();
		
		int [] size = rootMapping.getSize();
		
		packet.dataInt = new int [size [0] + 3];
		packet.dataFloat = new float [size [1]];
		packet.dataString = new String [size [2]];
		
		packet.dataInt [0] = tile.xCoord;
		packet.dataInt [1] = tile.yCoord;
		packet.dataInt [2] = tile.zCoord;
		
		try {
			rootMapping.setData(tile, packet.dataInt, packet.dataFloat,
					packet.dataString, 3, 0, 0);
			return packet;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	public void updateFromPacket (TileEntity tile, Packet230ModLoader packet) {
		try {
			rootMapping.updateFromData(tile, packet.dataInt, packet.dataFloat,
					packet.dataString, 3, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
