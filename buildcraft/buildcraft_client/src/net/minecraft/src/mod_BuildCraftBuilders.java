/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

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
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.FillerGUI));
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.TemplateGUI));
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.BuilderGUI));				
	}
	
	@Override
	public String Version() {
		return "2.2.1";
	}
	 
	public GuiScreen HandleGUI(int i) {		
		switch (Utils.intToPacketId(i)) {
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
