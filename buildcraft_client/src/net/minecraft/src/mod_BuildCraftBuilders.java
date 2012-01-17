/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.builders.GuiBuilder;
import net.minecraft.src.buildcraft.builders.GuiFiller;
import net.minecraft.src.buildcraft.builders.GuiTemplate;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileArchitect;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;

public class mod_BuildCraftBuilders extends BaseModMp {

	public static mod_BuildCraftBuilders instance;
	
	public mod_BuildCraftBuilders () {
		instance = this;
	}
	
	@Override
	public void load () {
		
	}
	
	@Override
	public void ModsLoaded () {
		super.ModsLoaded();
		
		BuildCraftBuilders.initialize();
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.FillerGUI));
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.TemplateGUI));
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.BuilderGUI));				
	}
	
	@Override
	public String getVersion() {
		return "3.1.2";
	}
	 
	@Override
	public GuiScreen HandleGUI(int i) {		
		switch (Utils.intToPacketId(i)) {
		case FillerGUI: 
			return new GuiFiller(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileFiller());
		case TemplateGUI:
			return new GuiTemplate(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileArchitect());
		case BuilderGUI:
			TileBuilder tile = new TileBuilder();
			tile.worldObj = ModLoader.getMinecraftInstance().theWorld;
			return new GuiBuilder(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					tile);
		default:
			return null;
		}
	}

}
