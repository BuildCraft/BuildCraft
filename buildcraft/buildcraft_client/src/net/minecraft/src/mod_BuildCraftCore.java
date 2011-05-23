package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;

public class mod_BuildCraftCore extends BaseMod {	
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();						
	}
	
	@Override
	public String Version() {
		return "1.5_01.5";
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	// map.put (EntityPassiveItem.class, new RenderItem());
    	map.put (EntityPassiveItem.class, new RenderPassiveItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());	
    }
}
