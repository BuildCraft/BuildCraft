package net.minecraft.src;

import net.minecraft.src.buildcraft.transport.GuiDiamondPipe;
import net.minecraft.src.buildcraft.transport.TileDiamondPipe;

public class mod_BuildCraftTransport extends BaseModMp {
		
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		
		ModLoaderMp.RegisterGUI(this, BuildCraftTransport.diamondGUI);
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
}
