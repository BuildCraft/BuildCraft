package net.minecraft.src;

import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;

public class mod_BuildCraftBuilders extends BaseModMp {

	public static mod_BuildCraftBuilders instance;
	
	public mod_BuildCraftBuilders () {
		instance = this;
	}
	
	public void ModsLoaded () {
		super.ModsLoaded();
		
		BuildCraftBuilders.initialize();
	}
	
	@Override
	public String Version() {
		return "1.6.6.3";
	}
	
	 public void HandlePacket(Packet230ModLoader packet) {    	
			switch (PacketIds.values() [packet.packetType]) {
			case MarkerDescription:
				Utils.handleDescriptionPacket(packet);
			case MarkerUpdate:
				Utils.handleUpdatePacket(packet);
			}		
	 }

}
