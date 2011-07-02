package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.GuiAutoCrafting;
import net.minecraft.src.buildcraft.factory.RenderMiningWell;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
		
		ModLoaderMp.RegisterGUI(this, PacketIds.AutoCraftingGUI.ordinal());
		instance = this;
	}
		
	@Override
	public String Version() {
		return "1.6.6.4";
	}
	    
	RenderMiningWell renderMiningWell = new RenderMiningWell();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void AddRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
	
	public void HandlePacket(Packet230ModLoader packet) {
		switch (PacketIds.values() [packet.packetType]) {
		case QuarryDescription:
			Utils.handleDescriptionPacket(packet);
		case QuarryUpdate:
			Utils.handleUpdatePacket(packet);
		}		
    }
	
    public GuiScreen HandleGUI(int i) {    	
    	if (i == PacketIds.AutoCraftingGUI.ordinal()) {
			return new GuiAutoCrafting(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					APIProxy.getWorld(),
					new TileAutoWorkbench());
    	} else {
    		return null;
    	}
    }
}
