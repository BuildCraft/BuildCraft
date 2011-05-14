package net.minecraft.src;

import net.minecraft.src.buildcraft.core.ITickListener;

public class mod_BuildCraftCore extends BaseModMp {	
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public mod_BuildCraftCore () {		
		mod_BuildCraftCore.initialize();
		
		ModLoader.SetInGameHook(this, true, false);											
	}
	
	@Override
	public String Version() {
		return "1.5_01.4";
	}
	    
    long lastTick = 0;
    
    public static void registerTicksListener (ITickListener listener, int pace) {
    	BuildCraftCore.registerTicksListener(listener, pace);
    }
    
    public void OnTickInGame()
    {   
    	proxy.OnTickInGame();
    }

	public static void unregisterTicksListener(ITickListener tilePipe) {
		BuildCraftCore.unregisterTicksListener(tilePipe);
	}
}
