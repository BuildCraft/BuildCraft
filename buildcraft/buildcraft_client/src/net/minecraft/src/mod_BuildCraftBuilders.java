package net.minecraft.src;

import net.minecraft.src.buildcraft.builders.GuiFiller;
import net.minecraft.src.buildcraft.builders.TileFiller;
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
		ModLoaderMp.RegisterGUI(this, PacketIds.FillerGUI.ordinal());
	}
	
	@Override
	public String Version() {
		return "1.6.6.3";
	}
	
	 public void HandlePacket(Packet230ModLoader packet) {    	
			switch (PacketIds.values() [packet.packetType]) {
			case MarkerDescription:
			case FillerDescription:
				Utils.handleDescriptionPacket(packet);
			case MarkerUpdate:
			case FillerUpdate:
				Utils.handleUpdatePacket(packet);
			
			}		
	 }
	 
	public GuiScreen HandleGUI(int i) {
		if (i == PacketIds.FillerGUI.ordinal()) {
			return new GuiFiller(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileFiller());
		} else {
			return null;
		}
	}

}
