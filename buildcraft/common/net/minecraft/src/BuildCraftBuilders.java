package net.minecraft.src;

import java.io.File;

import net.minecraft.src.buildcraft.api.FillerRegistry;
import net.minecraft.src.buildcraft.builders.BlockBuilder;
import net.minecraft.src.buildcraft.builders.BlockFiller;
import net.minecraft.src.buildcraft.builders.BlockMarker;
import net.minecraft.src.buildcraft.builders.BlockTemplate;
import net.minecraft.src.buildcraft.builders.FillerFillAll;
import net.minecraft.src.buildcraft.builders.FillerFillPyramid;
import net.minecraft.src.buildcraft.builders.FillerFillStairs;
import net.minecraft.src.buildcraft.builders.FillerFillWalls;
import net.minecraft.src.buildcraft.builders.FillerFlattener;
import net.minecraft.src.buildcraft.builders.FillerRemover;
import net.minecraft.src.buildcraft.builders.ItemTemplate;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileMarker;
import net.minecraft.src.buildcraft.builders.TileTemplate;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;

public class BuildCraftBuilders {
	public static BlockMarker markerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockTemplate templateBlock;
	public static ItemTemplate templateItem;
	
	public static void initialize () {		
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		templateItem = new ItemTemplate (Integer.parseInt(Utils.getProperty(
				"templateItem.id", "3805")));
		templateItem.setItemName("templateItem");
		CoreProxy.addName(templateItem, "Blank Template");
		craftingmanager.addRecipe(new ItemStack(templateItem, 1), new Object[] {
			"ppp", "pip", "ppp", 
			Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('p'), Item.paper });	
		
		markerBlock = new BlockMarker(Utils.getSafeBlockId("marker.blockId",
				154));		
		ModLoader.RegisterBlock(markerBlock);
		CoreProxy.addName(markerBlock.setBlockName("markerBlock"), "Land Mark");
		craftingmanager.addRecipe(new ItemStack(markerBlock, 1), new Object[] {
				"l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('r'),
				Block.torchRedstoneActive });	
		
		fillerBlock = new BlockFiller(Utils.getSafeBlockId("filler.blockId",
				155));		
		ModLoader.RegisterBlock(fillerBlock);
		CoreProxy.addName(fillerBlock.setBlockName("fillerBlock"), "Filler");
		craftingmanager.addRecipe(new ItemStack(fillerBlock, 1), new Object[] {
			"btb", "ycy", "gCg", 
			Character.valueOf('b'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('t'), markerBlock,
			Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
			Character.valueOf('c'), Block.workbench,
			Character.valueOf('g'), BuildCraftCore.goldGearItem,
			Character.valueOf('C'), Block.chest	});	
		
		builderBlock = new BlockBuilder(Utils.getSafeBlockId("builder.blockId",
				157));
		ModLoader.RegisterBlock(builderBlock);
		CoreProxy.addName(builderBlock.setBlockName("builderBlock"), "Builder");
		craftingmanager.addRecipe(new ItemStack(builderBlock, 1), new Object[] {
			"btb", "ycy", "gCg", 
			Character.valueOf('b'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('t'), markerBlock,
			Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
			Character.valueOf('c'), Block.workbench,
			Character.valueOf('g'), BuildCraftCore.diamondGearItem,
			Character.valueOf('C'), Block.chest	});	
		
		templateBlock = new BlockTemplate(Utils.getSafeBlockId("template.blockId",
				158));
		ModLoader.RegisterBlock(templateBlock);
		CoreProxy.addName(templateBlock.setBlockName("templateBlock"), "Template Drawing Table");
		craftingmanager.addRecipe(new ItemStack(templateBlock, 1), new Object[] {
			"btb", "ycy", "gCg", 
			Character.valueOf('b'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('t'), markerBlock,
			Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
			Character.valueOf('c'), Block.workbench,
			Character.valueOf('g'), BuildCraftCore.diamondGearItem,
			Character.valueOf('C'), new ItemStack (templateItem, 1) });	
		
		ModLoader.RegisterTileEntity(TileMarker.class, "Marker");
		ModLoader.RegisterTileEntity(TileFiller.class, "Filler");
		ModLoader.RegisterTileEntity(TileBuilder.class,
				"net.minecraft.src.builders.TileBuilder");
		ModLoader.RegisterTileEntity(TileTemplate.class,
				"net.minecraft.src.builders.TileTemplate");
		
		FillerRegistry.addRecipe(new FillerFillAll(), new Object[] { "bbb",
				"bbb", "bbb", Character.valueOf('b'),
				new ItemStack(Block.brick, 1) });
		
		FillerRegistry.addRecipe(new FillerFlattener(),
				new Object[] { "   ", "ggg", "bbb", Character.valueOf('g'),
						Block.glass, Character.valueOf('b'), Block.brick });
		
		FillerRegistry.addRecipe(new FillerRemover(),
				new Object[] { "ggg", "ggg", "ggg", Character.valueOf('g'),
						Block.glass });
		
		FillerRegistry.addRecipe(new FillerFillWalls(),
				new Object[] { "bbb", "b b", "bbb", Character.valueOf('b'),
						Block.brick });
		
		FillerRegistry.addRecipe(new FillerFillPyramid(),
				new Object[] { "   ", " b ", "bbb", Character.valueOf('b'),
						Block.brick });
		
		FillerRegistry.addRecipe(new FillerFillStairs(),
				new Object[] { "  b", " bb", "bbb", Character.valueOf('b'),
						Block.brick });

		
		Utils.saveProperties();
		
		loadBluePrints();
	}
		
	public static BluePrint bluePrints [] = new BluePrint [65025];
	
	public static int storeBluePrint (BluePrint bluePrint) {
		// Position 0 is "no blueprint yet"
		for (int i = 1; i < bluePrints.length; ++i) {
			if (bluePrints [i] == null) {
				bluePrints [i] = bluePrint;
				bluePrint.save(i);
				
				return i;
			}
		}
		
		throw new RuntimeException("No more blueprint slot available.");
	}

	
	public static void loadBluePrints () {
		File baseDir = CoreProxy.getBuildCraftBase();
		
		baseDir.mkdir();
		
		String files [] = baseDir.list();
		
		for (String file : files) {
			String [] parts = file.split("[.]");

			if (parts.length == 2) {
				if (parts[1].equals("bpt")) {
					int bptNumber = Integer.parseInt(parts[0]);
					
					if (bptNumber == 0) {
						continue;
					}
					
					bluePrints[bptNumber] = new BluePrint(
							new File(CoreProxy.getBuildCraftBase(), file));					
				}
			}
		}
	}

}
