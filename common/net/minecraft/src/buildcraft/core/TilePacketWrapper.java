/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.buildcraft.core.ClassMapping.Indexes;

public class TilePacketWrapper {
	
	ClassMapping rootMappings [];

	PacketIds packetType;
	
	@SuppressWarnings("rawtypes")
	public TilePacketWrapper (Class c, PacketIds packetType) {
		this (new Class [] {c}, packetType);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TilePacketWrapper (Class c [], PacketIds packetType) {
		rootMappings = new ClassMapping [c.length];
		
		for (int i = 0; i < c.length; ++i) {		
			rootMappings [i] = new ClassMapping(c [i]);
		}		
		
		this.packetType = packetType;
	}
	
	
	/*
	public Packet230ModLoader toPacket (TileEntity tile) {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = mod_BuildCraftCore.instance.getId();
		packet.isChunkDataPacket = true;
		packet.packetType = packetType.ordinal();
				
		int sizeF = 0, sizeS = 0;
		
		for (int i = 0; i < rootMappings.length; ++i) {					
			int [] size = rootMappings [i].getSize();
		
			sizeF += size [1];
			sizeS += size [2];
		}
		
		packet.dataFloat = new float [sizeF];
		packet.dataString = new String [sizeS];
		
		ByteBuffer buf = new ByteBuffer();
		
		buf.writeInt(tile.xCoord);
		buf.writeInt(tile.yCoord);
		buf.writeInt(tile.zCoord);
		
		try {			
			rootMappings [0].setData(tile, buf, packet.dataFloat,
					packet.dataString, new Indexes(0, 0));
			
			packet.dataInt = buf.readIntArray();
			
			return packet;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	public Packet230ModLoader toPacket (int x, int y, int z, Object obj) {
		return toPacket(x, y, z, new Object [] {obj});
	}
	
	public Packet230ModLoader toPacket (int x, int y, int z, Object [] obj) {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = mod_BuildCraftCore.instance.getId();
		packet.isChunkDataPacket = true;
		packet.packetType = packetType.ordinal();
		
		int sizeF = 0, sizeS = 0;
		
		for (int i = 0; i < rootMappings.length; ++i) {					
			int [] size = rootMappings [i].getSize();
		
			sizeF += size [1];
			sizeS += size [2];
		}
		
		packet.dataFloat = new float [sizeF];
		packet.dataString = new String [sizeS];
		
		ByteBuffer buf = new ByteBuffer();
		
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		
		try {
			Indexes ind = new Indexes(0, 0);
			
			for (int i = 0; i < rootMappings.length; ++i) {
				rootMappings [i].setData(obj [i], buf, packet.dataFloat,
						packet.dataString, ind);
			}
			
			packet.dataInt = buf.readIntArray();
			
			return packet;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}		
	}


	public void updateFromPacket (Object obj, Packet230ModLoader packet) {
		updateFromPacket(new Object [] {obj}, packet);
	}
	
	public void updateFromPacket (Object [] obj, Packet230ModLoader packet) {
		try {
			ByteBuffer buf = new ByteBuffer();
			buf.writeIntArray(packet.dataInt);
			buf.readInt();
			buf.readInt();
			buf.readInt();
			
			Indexes ind = new Indexes(0, 0);
			
			for (int i = 0; i < rootMappings.length; ++i) {
				rootMappings [i].updateFromData(obj [i], buf, packet.dataFloat,
						packet.dataString, ind);
			}
			
			packet.dataInt = buf.readIntArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateFromPacket (TileEntity tile, Packet230ModLoader packet) {
		try {
			ByteBuffer buf = new ByteBuffer();
			buf.writeIntArray(packet.dataInt);
			buf.readInt();
			buf.readInt();
			buf.readInt();
			
			rootMappings [0].updateFromData(tile, buf, packet.dataFloat,
					packet.dataString, new Indexes(0, 0));
			
			packet.dataInt = buf.readIntArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
