/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemBuildCraftTexture;
import net.minecraft.src.buildcraft.transport.BlockDockingStation;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.GuiHandler;
import net.minecraft.src.buildcraft.transport.LegacyBlock;
import net.minecraft.src.buildcraft.transport.LegacyTile;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicWood;
import net.minecraft.src.buildcraft.transport.TileDummyGenericPipe;
import net.minecraft.src.buildcraft.transport.TileDummyGenericPipe2;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.buildcraft.transport.network.ConnectionHandler;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsCobblestone;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsDiamond;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsGold;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsIron;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsObsidian;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsStone;
import net.minecraft.src.buildcraft.transport.pipes.PipeItemsWood;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsCobblestone;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsGold;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsIron;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsStone;
import net.minecraft.src.buildcraft.transport.pipes.PipeLiquidsWood;
import net.minecraft.src.buildcraft.transport.pipes.PipePowerGold;
import net.minecraft.src.buildcraft.transport.pipes.PipePowerStone;
import net.minecraft.src.buildcraft.transport.pipes.PipePowerWood;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

public class BuildCraftTransport {
	
	private static boolean initialized = false;
			
	public static BlockGenericPipe genericPipeBlock;
	public static BlockDockingStation dockingStationBlock;
	
	public static int [] diamondTextures = new int [6];
	
	public static boolean alwaysConnectPipes;
		
	public static Item pipeWaterproof;
	
	public static Item pipeItemsWood;
	public static Item pipeItemsStone;
	public static Item pipeItemsCobblestone;
	public static Item pipeItemsIron;
	public static Item pipeItemsGold;
	public static Item pipeItemsDiamond;
	public static Item pipeItemsObsidian;
	
	public static Item pipeLiquidsWood;
	public static Item pipeLiquidsCobblestone;
	public static Item pipeLiquidsStone;
	public static Item pipeLiquidsIron;
	public static Item pipeLiquidsGold;
		
	public static Item pipePowerWood;
	public static Item pipePowerStone;
	public static Item pipePowerGold;
	
	private static class PipeRecipe {
		ItemStack result;
		Object [] input;
	}
	
	private static LinkedList <PipeRecipe> pipeRecipes = new LinkedList <PipeRecipe> ();
	
	public static void load() {
		// Register connection handler
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());
	
		// Register gui handler
		MinecraftForge.setGuiHandler(mod_BuildCraftTransport.instance, new GuiHandler());
	}

	public static void initialize () {
		if (initialized) {
			return;
		}
		
		initialized = true;

		mod_BuildCraftCore.initialize();						
			
		Property loadLegacyPipes = BuildCraftCore.mainConfiguration
		.getOrCreateBooleanProperty("loadLegacyPipes", Configuration.CATEGORY_GENERAL, true);
		loadLegacyPipes.comment = "set to true to load pre 2.2.5 worlds pipes";		
		
		Property alwaysConnect = BuildCraftCore.mainConfiguration
				.getOrCreateBooleanProperty("pipes.alwaysConnect",
						Configuration.CATEGORY_GENERAL,
						DefaultProps.PIPES_ALWAYS_CONNECT);
		alwaysConnect.comment = "set to false to deactivate pipe connection rules, true by default";

		Property exclusionList = BuildCraftCore.mainConfiguration
				.getOrCreateProperty("woodenPipe.exclusion",
						Configuration.CATEGORY_BLOCK, "");

		PipeLogicWood.excludedBlocks = exclusionList.value.split(",");
		
		Property genericPipeId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("pipe.id",
				DefaultProps.GENERIC_PIPE_ID);
				
		Property dockingStationId = BuildCraftCore.mainConfiguration
		.getOrCreateBlockIdProperty("dockingStation.id",
				DefaultProps.DOCKING_STATION_ID);

		for (int j = 0; j < PipeLogicWood.excludedBlocks.length; ++j) {
			PipeLogicWood.excludedBlocks[j] = PipeLogicWood.excludedBlocks[j]
					.trim();
		}

		BuildCraftCore.mainConfiguration.save();		
		
		pipeWaterproof = new ItemBuildCraftTexture (DefaultProps.PIPE_WATERPROOF_ID).setIconIndex(2 * 16 + 1);
		pipeWaterproof.setItemName("pipeWaterproof");
		CoreProxy.addName(pipeWaterproof, "Pipe Waterproof");		
		genericPipeBlock = new BlockGenericPipe(Integer.parseInt(genericPipeId.value));
		
		// Fixing retro-compatiblity
		mod_BuildCraftTransport.registerTilePipe(TileDummyGenericPipe.class,
				"net.minecraft.src.buildcraft.GenericPipe");
		mod_BuildCraftTransport.registerTilePipe(TileDummyGenericPipe2.class,
				"net.minecraft.src.buildcraft.transport.TileGenericPipe");
		
		mod_BuildCraftTransport.registerTilePipe(TileGenericPipe.class,
				"net.minecraft.src.buildcraft.transport.GenericPipe");	
		
		pipeItemsWood = createPipe (DefaultProps.PIPE_ITEMS_WOOD_ID, PipeItemsWood.class, "Wooden Transport Pipe", Block.planks, Block.glass, Block.planks);
		pipeItemsCobblestone = createPipe(DefaultProps.PIPE_ITEMS_COBBLESTONE_ID, PipeItemsCobblestone.class, "Cobblestone Transport Pipe", Block.cobblestone, Block.glass, Block.cobblestone);
		pipeItemsStone = createPipe (DefaultProps.PIPE_ITEMS_STONE_ID, PipeItemsStone.class, "Stone Transport Pipe", Block.stone, Block.glass, Block.stone);
		pipeItemsIron = createPipe (DefaultProps.PIPE_ITEMS_IRON_ID, PipeItemsIron.class, "Iron Transport Pipe", Item.ingotIron, Block.glass, Item.ingotIron);
		pipeItemsGold = createPipe (DefaultProps.PIPE_ITEMS_GOLD_ID, PipeItemsGold.class, "Golden Transport Pipe", Item.ingotGold, Block.glass, Item.ingotGold);
		pipeItemsDiamond = createPipe (DefaultProps.PIPE_ITEMS_DIAMOND_ID, PipeItemsDiamond.class, "Diamond Transport Pipe", Item.diamond, Block.glass, Item.diamond);
		pipeItemsObsidian = createPipe (DefaultProps.PIPE_ITEMS_OBSIDIAN_ID, PipeItemsObsidian.class, "Obsidian Transport Pipe", Block.obsidian, Block.glass, Block.obsidian);
		
		pipeLiquidsWood = createPipe (DefaultProps.PIPE_LIQUIDS_WOOD_ID, PipeLiquidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood, null);
		pipeLiquidsCobblestone = createPipe (DefaultProps.PIPE_LIQUIDS_COBBLESTONE_ID, PipeLiquidsCobblestone.class, "Cobblestone Waterproof Pipe", pipeWaterproof, pipeItemsCobblestone, null);
		pipeLiquidsStone = createPipe (DefaultProps.PIPE_LIQUIDS_STONE_ID, PipeLiquidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone, null);		
		pipeLiquidsIron = createPipe (DefaultProps.PIPE_LIQUIDS_IRON_ID, PipeLiquidsIron.class, "Iron Waterproof Pipe", pipeWaterproof, pipeItemsIron, null);		
		pipeLiquidsGold = createPipe (DefaultProps.PIPE_LIQUIDS_GOLD_ID, PipeLiquidsGold.class, "Golden Waterproof Pipe", pipeWaterproof, pipeItemsGold, null);			
		// diamond		
		// obsidian
		
		pipePowerWood = createPipe (DefaultProps.PIPE_POWER_WOOD_ID, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone,  pipeItemsWood, null);		
		// cobblestone
		pipePowerStone = createPipe (DefaultProps.PIPE_POWER_STONE_ID, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone, null);		
		// iron
		pipePowerGold = createPipe(DefaultProps.PIPE_POWER_GOLD_ID, PipePowerGold.class, "Golden Conductive Pipe", Item.redstone, pipeItemsGold, null);
		// diamond
		// obsidian
		
//		dockingStationBlock = new BlockDockingStation(Integer.parseInt(dockingStationId.value));
//		ModLoader.RegisterBlock(dockingStationBlock);
//		CoreProxy.addName(dockingStationBlock.setBlockName("dockingStation"),
//		"Docking Station");
		
//		ModLoader.RegisterTileEntity(TileDockingStation.class, "net.minecraft.src.buildcraft.TileDockingStation");
		
		for (int j = 0; j < 6; ++j) {
			diamondTextures [j] = 1 * 16 + 6 + j;
		}				
		
		alwaysConnectPipes = Boolean.parseBoolean(alwaysConnect.value);
		
		if (loadLegacyPipes.value.equals("true")) {
			Property woodenPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("woodenPipe.id",
							DefaultProps.WOODEN_PIPE_ID);
			Property stonePipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("stonePipe.id",
							DefaultProps.STONE_PIPE_ID);
			Property ironPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("ironPipe.id",
							DefaultProps.IRON_PIPE_ID);
			Property goldenPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("goldenPipe.id",
							DefaultProps.GOLDEN_PIPE_ID);
			Property diamondPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("diamondPipe.id",
							DefaultProps.DIAMOND_PIPE_ID);
			Property obsidianPipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("obsidianPipe.id",
							DefaultProps.OBSIDIAN_PIPE_ID);
			Property cobblestonePipeId = BuildCraftCore.mainConfiguration
					.getOrCreateBlockIdProperty("cobblestonePipe.id",
							DefaultProps.COBBLESTONE_PIPE_ID);

			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(woodenPipeId.value), pipeItemsWood.shiftedIndex));
			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(stonePipeId.value), pipeItemsStone.shiftedIndex));
			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(ironPipeId.value), pipeItemsIron.shiftedIndex));
			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(goldenPipeId.value), pipeItemsGold.shiftedIndex));
			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(diamondPipeId.value), pipeItemsDiamond.shiftedIndex));
			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(obsidianPipeId.value), pipeItemsObsidian.shiftedIndex));
			ModLoader.registerBlock(new LegacyBlock(Integer
					.parseInt(cobblestonePipeId.value), pipeItemsCobblestone.shiftedIndex));

			ModLoader
					.registerTileEntity(LegacyTile.class,
							"net.buildcraft.src.buildcraft.transport.legacy.LegacyTile");
		}
		
		BuildCraftCore.mainConfiguration.save();
		
		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}	
	
	public static void loadRecipes () {
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		craftingmanager.addRecipe(new ItemStack(pipeWaterproof, 1), new Object[] {
			"W ", "  ", 
			Character.valueOf('W'), new ItemStack(Item.dyePowder, 1, 2)});
		
		for (PipeRecipe p : pipeRecipes) {
			craftingmanager.addRecipe(p.result, p.input);
		}
	}
	
	private static Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr, Object r1, Object r2, Object r3) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0))
				+ clas.getSimpleName().substring(1);
		
		Property prop = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty(name + ".id",
						Configuration.CATEGORY_ITEM, defaultID);
		
		int id = Integer.parseInt(prop.value);
		Item res =  BlockGenericPipe.registerPipe (id, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);
		
		PipeRecipe re = new PipeRecipe ();
		
		if (r1 != null && r2 != null && r3 != null) {	
			re.result = new ItemStack(res, 8);
			re.input = new Object[] {
				"   ", "ABC", "   ", 
				Character.valueOf('A'), r1,
				Character.valueOf('B'), r2,
				Character.valueOf('C'), r3};
			
			pipeRecipes.add(re);
		} else if (r1 != null && r2 != null) {
			re.result = new ItemStack(res, 1);
			re.input = new Object[] {
				"A ", "B ", 
				Character.valueOf('A'), r1,
				Character.valueOf('B'), r2};
			
			pipeRecipes.add(re);
		}
		
		return res;
	}
}
