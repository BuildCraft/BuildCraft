package net.minecraft.src;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;

public class mod_BuildCraftCore extends BaseModMp {	
	
	BuildCraftCore proxy = new BuildCraftCore();
		
	public static void initialize () {
		BuildCraftCore.initialize ();
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();												
	}
	
	@Override
	public String Version() {
		return "1.5_01.4";
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	map.put (EntityPassiveItem.class, new RenderItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
}
