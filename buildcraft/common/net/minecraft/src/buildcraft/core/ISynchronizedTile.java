/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

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
