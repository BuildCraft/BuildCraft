package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.RenderMiningWell;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.RenderTank;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileTank;

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
		
		instance = this;
	}
		
	@Override
	public String Version() {
		return "2.0.1";
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
