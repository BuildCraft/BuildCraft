package net.minecraft.src;

import net.minecraft.src.buildcraft.api.APIProxy;
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
		return "1.6.6.4";
	}
	
	 public void HandlePacket(Packet230ModLoader packet) {
		switch (PacketIds.values()[packet.packetType]) {
		case MarkerDescription:
		case FillerDescription:
		case BuilderDescription:
		case TemplateDescription:
			Utils.handleDescriptionPacket(packet);
		case MarkerUpdate:
		case FillerUpdate:
		case BuilderUpdate:
		case TemplateUpdate:
			Utils.handleUpdatePacket(packet);

		}		
	 }
	 
	public GuiScreen HandleGUI(int i) {		
		switch (PacketIds.values() [i]) {
		case FillerGUI: 
			return new GuiFiller(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileFiller());
		case TemplateGUI:
			return new GuiTemplate(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileTemplate());
		case BuilderGUI:
			TileBuilder tile = new TileBuilder();
			tile.worldObj = APIProxy.getWorld();
			return new GuiBuilder(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					tile);
		default:
			return null;
		}
	}

}
