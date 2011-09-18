package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.RenderMiningWell;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.RenderRefinery;
import net.minecraft.src.buildcraft.factory.RenderTank;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileRefinery;
import net.minecraft.src.buildcraft.factory.TileTank;
import net.minecraft.src.mod_BuildCraftCore.EntityRenderIndex;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
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
	public String Version() {
		return "2.2.0";
	}
	    
	RenderMiningWell renderMiningWell = new RenderMiningWell();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void AddRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
	
    public GuiScreen HandleGUI(int i) {    	
    	if (Utils.intToPacketId(i) == PacketIds.AutoCraftingGUI) {
			return new GuiAutoCrafting(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					APIProxy.getWorld(),
					new TileAutoWorkbench());
    	} else {
    		return null;
    	}
    }
}
