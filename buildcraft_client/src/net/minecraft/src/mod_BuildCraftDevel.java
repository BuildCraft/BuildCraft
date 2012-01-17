/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import net.minecraft.src.buildcraft.devel.BlockCheat;
import net.minecraft.src.forge.Property;

public class mod_BuildCraftDevel extends BaseModMp {	

	public static BlockCheat cheatBlock;
	
	@Override
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
	public String getVersion() {
		return "3.1.2";
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}
}
