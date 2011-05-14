package net.minecraft.src;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.ITickListener;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;

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
    
    public void OnTickInGame(Minecraft minecraft)
    {   
    	proxy.OnTickInGame();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	map.put (EntityPassiveItem.class, new RenderItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }

	public static void unregisterTicksListener(ITickListener tilePipe) {
		BuildCraftCore.unregisterTicksListener(tilePipe);
	}
}
