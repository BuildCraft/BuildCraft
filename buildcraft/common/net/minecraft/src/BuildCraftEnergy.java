package net.minecraft.src;

import net.minecraft.src.buildcraft.energy.BlockEngine;
import net.minecraft.src.buildcraft.energy.ItemEngine;
import net.minecraft.src.buildcraft.energy.TileEngine;

public class BuildCraftEnergy {
	public static BlockEngine engineBlock;
	
	public static void ModsLoaded () {		
		engineBlock = new BlockEngine(200);
		ModLoader.RegisterBlock(engineBlock);
		ModLoader.RegisterTileEntity(TileEngine.class,
		"net.minecraft.src.buildcraft.energy.Engine");
		Item.itemsList[engineBlock.blockID] = (new ItemEngine(
				engineBlock.blockID - 256));		
	}

}
