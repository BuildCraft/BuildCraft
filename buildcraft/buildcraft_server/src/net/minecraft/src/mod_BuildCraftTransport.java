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
		return "1.7.2.1";
	}    
}
