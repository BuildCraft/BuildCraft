package net.minecraft.src;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.transport.GuiDiamondPipe;
import net.minecraft.src.buildcraft.transport.TileDiamondPipe;
import net.minecraft.src.buildcraft.transport.TilePipe;

public class mod_BuildCraftTransport extends BaseModMp {
		
	public static mod_BuildCraftTransport instance;

	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		
		ModLoaderMp.RegisterGUI(this, BuildCraftTransport.diamondGUI);
		
		instance = this;
	}
	
	@Override
	public String Version() {
		return "1.6.6.1";
	}
	
    public GuiScreen HandleGUI(int i)
    {    	
    	if (i == BuildCraftTransport.diamondGUI) {
			return new GuiDiamondPipe(
					ModLoader.getMinecraftInstance().thePlayer.inventory,
					new TileDiamondPipe());
    	} else {
    		return null;
    	}
    }
    
    public void HandlePacket(Packet230ModLoader packet) {
		int x = packet.dataInt [0];
		int y = packet.dataInt [1];
		int z = packet.dataInt [2];
		
		if (packet.packetType == BuildCraftTransport.tilePipeItemPacket) {						
			if (APIProxy.getWorld().blockExists(x, y, z)) {
				TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
				
				if (tile instanceof TilePipe) {
					((TilePipe) tile).handleItemPacket(packet);	
					
					return;
				}
			}
		}				
    }
}
