package net.minecraft.src;

public class mod_BuildCraftTransport extends BaseMod {
	
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();	
	}
	
	@Override
	public String Version() {
		return "1.6.4.1";
	}
}
