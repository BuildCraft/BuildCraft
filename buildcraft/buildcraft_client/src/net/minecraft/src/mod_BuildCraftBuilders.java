package net.minecraft.src;

import net.minecraft.src.buildcraft.builders.GuiBuilder;
import net.minecraft.src.buildcraft.builders.GuiFiller;
import net.minecraft.src.buildcraft.builders.GuiTemplate;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileTemplate;
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
		ModLoaderMp.RegisterGUI(this, PacketIds.TemplateGUI.ordinal());
		ModLoaderMp.RegisterGUI(this, PacketIds.BuilderGUI.ordinal());		
	}
	
	@Override
	public String Version() {
		return "1.6.6.3";
	}
	
	 public void HandlePacket(Packet230ModLoader packet) {    	
			switch (PacketIds.values() [packet.packetType]) {
			case MarkerDescription:
			case FillerDescription:
			case BuilderDescription:
				Utils.handleDescriptionPacket(packet);
			case MarkerUpdate:
			case FillerUpdate:
			case BuilderUpdate:
				Utils.handleUpdatePacket(packet);
			
			}		
	 }
	 
	public GuiScreen HandleGUI(int i) {
		if (i == PacketIds.FillerGUI.ordinal()) {
			return new GuiFiller(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileFiller());
		} else if (i == PacketIds.TemplateGUI.ordinal()) {
			return new GuiTemplate(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileTemplate());
		} else if (i == PacketIds.BuilderGUI.ordinal()) {
			return new GuiBuilder(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileBuilder());
		} else {
			return null;
		}
	}

}
