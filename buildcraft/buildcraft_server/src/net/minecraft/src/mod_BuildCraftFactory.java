package net.minecraft.src;

public class mod_BuildCraftFactory extends BaseModMp {		
	
	public static mod_BuildCraftFactory instance;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
		
		instance = this;
	}
		
	@Override
	public String Version() {
		return "2.1.0";
	}
}
