package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;

public interface ISynchronizedTile {
	public void handleDescriptionPacket (Packet230ModLoader packet);
	
	public void handleUpdatePacket (Packet230ModLoader packet);
	
	public void postPacketHandling (Packet230ModLoader packet);
	
	public Packet230ModLoader getUpdatePacket ();
	
	public Packet getDescriptionPacket ();
}
