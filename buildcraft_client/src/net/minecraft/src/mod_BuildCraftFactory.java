/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.mod_BuildCraftCore.EntityRenderIndex;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.RenderRefinery;
import net.minecraft.src.buildcraft.factory.RenderTank;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileRefinery;
import net.minecraft.src.buildcraft.factory.TileTank;
import net.minecraft.src.forge.NetworkMod;

public class mod_BuildCraftFactory extends NetworkMod {		
	
	public static mod_BuildCraftFactory instance;
	
	public mod_BuildCraftFactory() {
		instance = this;
	}
	
	@Override
	public void modsLoaded () {		
		super.modsLoaded();
		
		BuildCraftFactory.initialize();
				
		ModLoader
		.registerTileEntity(TileTank.class,
				"net.minecraft.src.buildcraft.factory.TileTank",
				new RenderTank());
		
		ModLoader.registerTileEntity(TileRefinery.class,
				"net.minecraft.src.buildcraft.factory.Refinery",
				new RenderRefinery());
		
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftFactory.refineryBlock, 0), new RenderRefinery());	
		
	}
		
	@Override
	public String getVersion() {
		return DefaultProps.VERSION;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
	
	@Override
	public void load() {
		BuildCraftFactory.load();
		}
	
	@Override public boolean clientSideRequired() { return true; }
	@Override public boolean serverSideRequired() { return false; }
}
