package net.minecraft.src;

import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.GuiDiamondPipe;
import net.minecraft.src.buildcraft.transport.TileDiamondPipe;
import net.minecraft.src.buildcraft.transport.TilePipe;

public class mod_BuildCraftTransport extends BaseModMp {
		
	public static mod_BuildCraftTransport instance;

	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		
		ModLoaderMp.RegisterGUI(this,
				Utils.packetIdToInt(PacketIds.DiamondPipeGUI));
		
		instance = this;
	}
	
	@Override
	public String Version() {
		return "2.0.0";
	}
	
    public GuiScreen HandleGUI(int i) {    	
    	if (Utils.intToPacketId(i) == PacketIds.DiamondPipeGUI) {
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
		
		if (packet.packetType == PacketIds.PipeItem.ordinal()) {						
			if (APIProxy.getWorld().blockExists(x, y, z)) {
				TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
				
				if (tile instanceof TilePipe) {
					((TilePipe) tile).handleItemPacket(packet);	
					
					return;
				}
			}
		} else if (packet.packetType == PacketIds.DiamondPipeContents.ordinal()) {	
			if (APIProxy.getWorld().blockExists(x, y, z)) {
				TileEntity tile = APIProxy.getWorld().getBlockTileEntity(x, y, z);
				
				if (tile instanceof TileDiamondPipe) {
					((TileDiamondPipe) tile).handlePacket(packet);	
					
					return;
				}
			}
			
			BlockIndex index = new BlockIndex(x, y, z);
			
			if (BuildCraftCore.bufferedDescriptions.containsKey(index)) {
				BuildCraftCore.bufferedDescriptions.remove(index);
			}			
			
			BuildCraftCore.bufferedDescriptions.put(index, packet);
		}			
    }
}
