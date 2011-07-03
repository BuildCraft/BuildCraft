package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.energy.BlockEngine;
import net.minecraft.src.buildcraft.energy.BlockOilFlowing;
import net.minecraft.src.buildcraft.energy.BlockOilStill;
import net.minecraft.src.buildcraft.energy.ItemBuildCraftBucket;
import net.minecraft.src.buildcraft.energy.ItemEngine;
import net.minecraft.src.buildcraft.energy.TileEngine;

public class BuildCraftEnergy {
	public static BlockEngine engineBlock;
	
	public static Block oilMoving;
	public static Block oilStill;
	public static Item bucketOil;
	
	public static void ModsLoaded () {		
		engineBlock = new BlockEngine(200);
		ModLoader.RegisterBlock(engineBlock);
		ModLoader.RegisterTileEntity(TileEngine.class,
		"net.minecraft.src.buildcraft.energy.Engine");
		Item.itemsList[engineBlock.blockID] = (new ItemEngine(
				engineBlock.blockID - 256));
		
		oilMoving = (new BlockOilFlowing(201, Material.water)).setHardness(100F).setLightOpacity(3).setBlockName("oil");
		CoreProxy.addName(oilMoving.setBlockName("oilMoving"), "Oil");
		ModLoader.RegisterBlock(oilMoving);
        oilStill = (new BlockOilStill(202, Material.water)).setHardness(100F).setLightOpacity(3).setBlockName("oil");
        CoreProxy.addName(oilStill.setBlockName("oilStill"), "Oil");
        ModLoader.RegisterBlock(oilStill);
        
        Item.bucketEmpty = (new ItemBuildCraftBucket(69, 0)).setIconCoord(10, 4).setItemName("bucket");
        
		bucketOil = (new ItemBucket(71, oilMoving.blockID))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/energy/gui/oil_bucket.png"))
				.setItemName("bucketOil").setContainerItem(Item.bucketEmpty);
		CoreProxy.addName(bucketOil, "Oil Bucket");
	}

}
