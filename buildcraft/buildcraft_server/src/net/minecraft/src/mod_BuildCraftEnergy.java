package net.minecraft.src;

public class mod_BuildCraftEnergy extends BaseModMp {

	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftEnergy.ModsLoaded();	
	}
	
	@Override
	public String Version() {
		return "1.6.6.4";
	}
}
