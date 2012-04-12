/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import java.io.File;

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
import net.minecraft.src.buildcraft.builders.GuiHandler;
import net.minecraft.src.buildcraft.builders.ItemTemplate;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileMarker;
import net.minecraft.src.buildcraft.builders.TileTemplate;
import net.minecraft.src.buildcraft.core.BluePrint;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.FillerRegistry;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

public class BuildCraftBuilders {
	public static BlockMarker markerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockTemplate templateBlock;
	public static ItemTemplate templateItem;
	
	private static boolean initialized = false;
	
	public static void load() {
		// Register gui handler
		MinecraftForge.setGuiHandler(mod_BuildCraftBuilders.instance, new GuiHandler());
	}

	public static void initialize () {	
		if (initialized) {
			return;
		} else {
			initialized = true;
		}
		
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();
		
		Property templateItemId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("templateItem.id", Configuration.CATEGORY_ITEM,
						DefaultProps.TEMPLATE_ITEM_ID);
		Property markerId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("marker.id", DefaultProps.MARKER_ID);
		Property fillerId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("filler.id", DefaultProps.FILLER_ID);
		Property builderId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("builder.id",
						DefaultProps.BUILDER_ID);
		Property templateId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("template.id",
						DefaultProps.TEMPLATE_ID);
		
		BuildCraftCore.mainConfiguration.save();		
		
		templateItem = new ItemTemplate (Integer.parseInt(templateItemId.value));
		templateItem.setItemName("templateItem");
		CoreProxy.addName(templateItem, "Blank Template");
		
		markerBlock = new BlockMarker(Integer.parseInt(markerId.value));		
		ModLoader.registerBlock(markerBlock);
		CoreProxy.addName(markerBlock.setBlockName("markerBlock"), "Land Mark");
		
		fillerBlock = new BlockFiller(Integer.parseInt(fillerId.value));		
		ModLoader.registerBlock(fillerBlock);
		CoreProxy.addName(fillerBlock.setBlockName("fillerBlock"), "Filler");
	
		builderBlock = new BlockBuilder(Integer.parseInt(builderId.value));
		ModLoader.registerBlock(builderBlock);
		CoreProxy.addName(builderBlock.setBlockName("builderBlock"), "Builder");
		
		templateBlock = new BlockTemplate(Integer.parseInt(templateId.value));
		ModLoader.registerBlock(templateBlock);
		CoreProxy.addName(templateBlock.setBlockName("templateBlock"), "Template Drawing Table");
		
		ModLoader.registerTileEntity(TileMarker.class, "Marker");
		ModLoader.registerTileEntity(TileFiller.class, "Filler");
		ModLoader.registerTileEntity(TileBuilder.class,
				"net.minecraft.src.builders.TileBuilder");
		ModLoader.registerTileEntity(TileTemplate.class,
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

		
		BuildCraftCore.mainConfiguration.save();
		
		loadBluePrints();
		
		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}
	
	public static void loadRecipes () {
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		craftingmanager.addRecipe(new ItemStack(templateItem, 1), new Object[] {
			"ppp", "pip", "ppp", 
			Character.valueOf('i'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('p'), Item.paper });	
		
		craftingmanager.addRecipe(new ItemStack(markerBlock, 1), new Object[] {
			"l ", "r ", Character.valueOf('l'),
			new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('r'),
			Block.torchRedstoneActive });	
		
		craftingmanager.addRecipe(new ItemStack(fillerBlock, 1), new Object[] {
			"btb", "ycy", "gCg", 
			Character.valueOf('b'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('t'), markerBlock,
			Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
			Character.valueOf('c'), Block.workbench,
			Character.valueOf('g'), BuildCraftCore.goldGearItem,
			Character.valueOf('C'), Block.chest	});	
		
		craftingmanager.addRecipe(new ItemStack(builderBlock, 1), new Object[] {
			"btb", "ycy", "gCg", 
			Character.valueOf('b'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('t'), markerBlock,
			Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
			Character.valueOf('c'), Block.workbench,
			Character.valueOf('g'), BuildCraftCore.diamondGearItem,
			Character.valueOf('C'), Block.chest	});	
		
		craftingmanager.addRecipe(new ItemStack(templateBlock, 1), new Object[] {
			"btb", "ycy", "gCg", 
			Character.valueOf('b'), new ItemStack(Item.dyePowder, 1, 0),
			Character.valueOf('t'), markerBlock,
			Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
			Character.valueOf('c'), Block.workbench,
			Character.valueOf('g'), BuildCraftCore.diamondGearItem,
			Character.valueOf('C'), new ItemStack (templateItem, 1) });	
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
		File baseDir = new File (CoreProxy.getBuildCraftBase(), "blueprints/");
		
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
							new File(baseDir, file));					
				}
			}
		}
	}
}
