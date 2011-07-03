package net.minecraft.src;

public class mod_BuildCraftEnergy extends BaseModMp {

	public static mod_BuildCraftEnergy instance;
	
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftEnergy.ModsLoaded();	
		
		instance = this;
	}
	
	@Override
	public String Version() {
		return "1.6.6.4";
	}
}
