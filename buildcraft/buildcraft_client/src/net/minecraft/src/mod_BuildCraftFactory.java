package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.factory.EntityModel;
import net.minecraft.src.buildcraft.factory.RenderMiningWell;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;

public class mod_BuildCraftFactory extends BaseMod {		
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		BuildCraftFactory.initialize();
	}
		
	@Override
	public String Version() {
		return "1.6.6.1";
	}
	    
	RenderMiningWell renderMiningWell = new RenderMiningWell();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void AddRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());    
    	
    	map.put (EntityModel.class, renderMiningWell);
		mod_BuildCraftCore.blockByEntityRenders.put(BuildCraftFactory.miningWellBlock,
				renderMiningWell);
    }
}
