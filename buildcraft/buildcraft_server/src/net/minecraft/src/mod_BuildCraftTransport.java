package net.minecraft.src;

public class mod_BuildCraftTransport extends BaseModMp {
	
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		BuildCraftTransport.initializeModel(this);		
	}
	
		
	@Override
	public String Version() {
		return "1.5_01.4";
	}    
}
