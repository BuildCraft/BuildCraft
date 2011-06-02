package net.minecraft.src.buildcraft.api;

import net.minecraft.src.Packet;

public interface ITrackedEntity {

	public Packet getSpawnPacket ();
	
	public Packet getUpdatePacket ();
	
	public int getUpdateFrequency ();
	
	public int getMaximumDistance ();
	
}
