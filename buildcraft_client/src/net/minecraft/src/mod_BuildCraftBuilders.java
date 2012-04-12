/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.builders.GuiBuilder;
import net.minecraft.src.buildcraft.builders.GuiFiller;
import net.minecraft.src.buildcraft.builders.GuiTemplate;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileTemplate;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftBuilders extends NetworkMod {

	public static mod_BuildCraftBuilders instance;
	
	public mod_BuildCraftBuilders () {
		instance = this;
	}
	
	@Override
	public void load () {
		BuildCraftBuilders.load();
	}
	
	@Override
	public void modsLoaded () {
		super.modsLoaded();
		
		BuildCraftBuilders.initialize();
	}
	
	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}
	
	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return false; }
}
