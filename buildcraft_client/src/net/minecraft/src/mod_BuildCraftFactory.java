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

import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.RenderRefinery;
import net.minecraft.src.buildcraft.factory.RenderTank;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileRefinery;
import net.minecraft.src.buildcraft.factory.TileTank;
import net.minecraft.src.mod_BuildCraftCore.EntityRenderIndex;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
	@Override
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
		
		ModLoaderMp.RegisterGUI(this, Utils.packetIdToInt(PacketIds.AutoCraftingGUI));
		
		ModLoader
		.RegisterTileEntity(TileTank.class,
				"net.minecraft.src.buildcraft.factory.TileTank",
				new RenderTank());
		
		ModLoader.RegisterTileEntity(TileRefinery.class,
				"net.minecraft.src.buildcraft.factory.Refinery",
				new RenderRefinery());
		
		mod_BuildCraftCore.blockByEntityRenders.put(new EntityRenderIndex(
				BuildCraftFactory.refineryBlock, 0), new RenderRefinery());	
		
		instance = this;
	}
		
	@Override
	public String getVersion() {
		return "3.1.2";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void AddRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
	
	@Override
    public GuiScreen HandleGUI(int i) {    	
    	if (Utils.intToPacketId(i) == PacketIds.AutoCraftingGUI) {
			return new GuiAutoCrafting(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					ModLoader.getMinecraftInstance().theWorld,
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
