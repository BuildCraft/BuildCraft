package net.minecraft.src;

public class mod_BuildCraftFactory extends BaseMod {		
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
	}
		
	@Override
	public String Version() {
		return "1.6.6.1";
	}
}
