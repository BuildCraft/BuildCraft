package net.minecraft.src;

import net.minecraft.src.buildcraft.devel.BlockCheat;
import net.minecraft.src.forge.Configuration.Property;

public class mod_BuildCraftDevel extends BaseModMp {	

	public static BlockCheat cheatBlock;
	
    public void ModsLoaded() {	
    	super.ModsLoaded();
    	
		mod_BuildCraftCore.initialize();
		
		Property cheatId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("cheat.id", 255);
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		cheatBlock = new BlockCheat(Integer.parseInt(cheatId.value));
		ModLoader.RegisterBlock(cheatBlock);
		craftingmanager.addRecipe(new ItemStack(cheatBlock, 1), new Object[] {
			"# ", "  ", Character.valueOf('#'), Block.dirt });
		
		BuildCraftCore.mainConfiguration.save();
	}
	
	@Override
	public String Version() {
		return "2.0.0";
	}
}
