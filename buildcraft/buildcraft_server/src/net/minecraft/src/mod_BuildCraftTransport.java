package net.minecraft.src;

public class mod_BuildCraftTransport extends BaseModMp {
	
	public static mod_BuildCraftTransport instance;
	
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		
		instance = this;
	}
	
		
	@Override
	public String Version() {
		return "2.0.1";
	}    
}
