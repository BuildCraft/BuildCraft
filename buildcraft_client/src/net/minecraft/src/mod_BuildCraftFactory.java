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
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.RenderRefinery;
import net.minecraft.src.buildcraft.factory.RenderTank;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileRefinery;
import net.minecraft.src.buildcraft.factory.TileTank;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
	@Override
	public void modsLoaded () {		
		super.modsLoaded();
		
		BuildCraftFactory.initialize();
		
		ModLoaderMp.registerGUI(this, Utils.packetIdToInt(PacketIds.AutoCraftingGUI));
		
		ModLoader
		.registerTileEntity(TileTank.class,
				"net.minecraft.src.buildcraft.factory.TileTank",
				new RenderTank());
		
		ModLoader.registerTileEntity(TileRefinery.class,
				"net.minecraft.src.buildcraft.factory.Refinery",
				new RenderRefinery());
		
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftFactory.refineryBlock, 0), new RenderRefinery());	
		
		instance = this;
	}
		
	@Override
	public String getVersion() {
		return "2.2.13";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
	
	@Override
    public GuiScreen handleGUI(int i) {    	
    	if (Utils.intToPacketId(i) == PacketIds.AutoCraftingGUI) {
			return new GuiAutoCrafting(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					APIProxy.getWorld(),
					new TileAutoWorkbench());
    	} else {
    		return null;
    	}
    }

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}
}
