package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;

public class mod_BuildCraftCore extends BaseMod {	
	
	BuildCraftCore proxy = new BuildCraftCore();
	
	public static HashMap<Block, Render> blockByEntityRenders = 
		new HashMap<Block, Render>();
		
	public static void initialize () {
		BuildCraftCore.initialize ();	
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
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
    
    public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			renderblocks.renderStandardBlock(block, i, j, k);
			
			return true;
		}
		
		return false;
    }
	
    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j) {
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel
				&& blockByEntityRenders.containsKey(block)) {
    		//  ??? GET THE ENTITY FROM THE TILE
			blockByEntityRenders.get(block).doRender(null, -0.5, -0.5, -0.5, 0,
					0);
		}
    }
}
