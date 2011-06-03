package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.factory.EntityModel;
import net.minecraft.src.buildcraft.factory.RenderMiningWell;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.TileQuarry;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
		
		instance = this;
	}
		
	@Override
	public String Version() {
		return "1.6.6.1";
	}
	    
	RenderMiningWell renderMiningWell = new RenderMiningWell();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void AddRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());    
    	
    	map.put (EntityModel.class, renderMiningWell);
		mod_BuildCraftCore.blockByEntityRenders.put(BuildCraftFactory.miningWellBlock,
				renderMiningWell);
    }
	
	public void HandlePacket(Packet230ModLoader packet) {
		int x = packet.dataInt [0];
		int y = packet.dataInt [1];
		int z = packet.dataInt [2];
		
		if (packet.packetType == BuildCraftFactory.tileQuarryDescriptionPacket) {						
			if (APIProxy.getWorld().blockExists(x, y, z)) {
				TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
				
				if (tile instanceof TileQuarry) {
					((TileQuarry) tile).handleDescriptionPacket(packet);	
					
					return;
				}
			}
		}
		
		BlockIndex index = new BlockIndex(x, y, z);
		
		if (BuildCraftCore.bufferedDescriptions.containsKey(index)) {
			BuildCraftCore.bufferedDescriptions.remove(index);
		}
		
		BuildCraftCore.bufferedDescriptions.put(index, packet);
    }
}
