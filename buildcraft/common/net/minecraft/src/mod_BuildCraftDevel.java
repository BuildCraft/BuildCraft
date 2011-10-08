/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.devel.BlockCheat;
import net.minecraft.src.forge.Property;

public class mod_BuildCraftDevel extends BaseModMp {	

	public static BlockCheat cheatBlock;
	
    public void ModsLoaded() {	
    	super.ModsLoaded();
    	
    	BuildCraftCore.debugMode = true;
    	
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
		return "2.2.1";
	}
}
