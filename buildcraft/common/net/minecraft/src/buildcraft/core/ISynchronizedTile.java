package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Packet230ModLoader;

public interface ISynchronizedTile {
	public void handleDescriptionPacket (Packet230ModLoader packet);
	
	public void handleUpdatePacket (Packet230ModLoader packet);
}
