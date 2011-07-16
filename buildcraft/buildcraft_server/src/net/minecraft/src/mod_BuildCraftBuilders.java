package net.minecraft.src;

public class mod_BuildCraftBuilders extends BaseModMp {

	public static mod_BuildCraftBuilders instance;
	
	public mod_BuildCraftBuilders () {
		instance = this;
	}
	
	public void ModsLoaded () {
		super.ModsLoaded();
		
		BuildCraftBuilders.initialize();
	}
	
	@Override
	public String Version() {
		return "1.7.3.2";
	}

}
