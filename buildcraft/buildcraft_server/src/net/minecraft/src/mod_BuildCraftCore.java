package net.minecraft.src;

public class mod_BuildCraftCore extends BaseModMp {	
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
	}
	
	@Override
	public String Version() {
		return version ();
	}
	
	public static String version() {
		return "1.6.6.3";
	}
}
