package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.builders.BlockMarker;
import net.minecraft.src.buildcraft.builders.TileMarker;

public class mod_BuildCraftBuilders extends BaseMod {

	public static BlockMarker markerBlock;
	
	public void ModsLoaded () {		
		super.ModsLoaded();
		
		mod_BuildCraftCore.initialize();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		markerBlock = new BlockMarker(Utils.getSafeBlockId("marker.blockId",
				154));
		
		ModLoader.RegisterBlock(markerBlock);
		CoreProxy.addName(markerBlock.setBlockName("markerBlock"), "Marker");
		craftingmanager.addRecipe(new ItemStack(markerBlock, 64), new Object[] {
			"ii", "  ", Character.valueOf('i'), Block.dirt });	
		
		ModLoader.RegisterTileEntity(TileMarker.class, "Marker");
		

	}
	
	@Override
	public String Version() {
		// TODO Auto-generated method stub
		return "1.5_01.5";
	}

}
