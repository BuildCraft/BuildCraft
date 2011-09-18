package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemBuildCraftTexture;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.LegacyBlock;
import net.minecraft.src.buildcraft.transport.LegacyTile;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicWood;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
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
import net.minecraft.src.forge.Property;

public class BuildCraftTransport {
	
	private static boolean initialized = false;
			
	public static BlockGenericPipe genericPipeBlock;
	
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
	
	public static void initialize () {
		if (initialized) {
			return;
		}
		
		initialized = true;
		
		mod_BuildCraftCore.initialize();						
			
		Property loadLegacyPipes = BuildCraftCore.mainConfiguration
		.getOrCreateBooleanProperty("loadLegacyPipes", Configuration.GENERAL_PROPERTY, true);
		loadLegacyPipes.comment = "set to true to load pre 2.2.0 worlds pipes";		
		
		Property alwaysConnect = BuildCraftCore.mainConfiguration
				.getOrCreateBooleanProperty("pipes.alwaysConnect",
						Configuration.GENERAL_PROPERTY,
						DefaultProps.PIPES_ALWAYS_CONNECT);
		alwaysConnect.comment = "set to false to deactivate pipe connection rules, true by default";

		Property exclusionList = BuildCraftCore.mainConfiguration
				.getOrCreateProperty("woodenPipe.exclusion",
						Configuration.BLOCK_PROPERTY, "");

		PipeLogicWood.excludedBlocks = exclusionList.value.split(",");

		for (int j = 0; j < PipeLogicWood.excludedBlocks.length; ++j) {
			PipeLogicWood.excludedBlocks[j] = PipeLogicWood.excludedBlocks[j]
					.trim();
		}

		BuildCraftCore.mainConfiguration.save();

		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		pipeWaterproof = new ItemBuildCraftTexture (DefaultProps.PIPE_WATERPROOF_ID).setIconIndex(2 * 16 + 1);
		pipeWaterproof.setItemName("pipeWaterproof");
		CoreProxy.addName(pipeWaterproof, "Pipe Waterproof");
		craftingmanager.addRecipe(new ItemStack(pipeWaterproof, 1), new Object[] {
			"W ", "  ", 
			Character.valueOf('W'), new ItemStack(Item.dyePowder, 1, 2)});
		
		genericPipeBlock = new BlockGenericPipe(166);
		
		pipeItemsWood = createPipe (4050, PipeItemsWood.class, "Wooden Transport Pipe", Block.planks, Block.glass, Block.planks);
		pipeItemsCobblestone = createPipe(4051, PipeItemsCobblestone.class, "Cobblestone Transport Pipe", Block.cobblestone, Block.glass, Block.cobblestone);
		pipeItemsStone = createPipe (4052, PipeItemsStone.class, "Stone Transport Pipe", Block.stone, Block.glass, Block.stone);
		pipeItemsIron = createPipe (4053, PipeItemsIron.class, "Iron Transport Pipe", Item.ingotIron, Block.glass, Item.ingotIron);
		pipeItemsGold = createPipe (4054, PipeItemsGold.class, "Golden Transport Pipe", Item.ingotGold, Block.glass, Item.ingotGold);
		pipeItemsDiamond = createPipe (4055, PipeItemsDiamond.class, "Diamond Transport Pipe", Item.diamond, Block.glass, Item.diamond);
		pipeItemsObsidian = createPipe (4056, PipeItemsObsidian.class, "Obsidian Transport Pipe", Block.obsidian, Block.glass, Block.obsidian);
		
		pipeLiquidsWood = createPipe (4057, PipeLiquidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood, null);
		pipeLiquidsCobblestone = createPipe (4058, PipeLiquidsCobblestone.class, "Cobblestone Waterproof Pipe", pipeWaterproof, pipeItemsCobblestone, null);
		pipeLiquidsStone = createPipe (4059, PipeLiquidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone, null);		
		pipeLiquidsIron = createPipe (4060, PipeLiquidsIron.class, "Iron Waterproof Pipe", pipeWaterproof, pipeItemsIron, null);		
		pipeLiquidsGold = createPipe (4061, PipeLiquidsGold.class, "Golden Waterproof Pipe", pipeWaterproof, pipeItemsGold, null);			
		// diamond 4062		
		
		pipePowerWood = createPipe (4063, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone,  pipeItemsWood, null);		
		// cobblestone 4064
		pipePowerStone = createPipe (4065, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone, null);		
		// iron 4066
		pipePowerGold = createPipe(4067, PipePowerGold.class, "Golden Conductive Pipe", Item.redstone, pipeItemsGold, null);
		// diamond 4068
		
		ModLoader.RegisterTileEntity(TileGenericPipe.class,
				"net.minecraft.src.buildcraft.GenericPipe");
		
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

			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(woodenPipeId.value), pipeItemsWood.shiftedIndex));
			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(stonePipeId.value), pipeItemsStone.shiftedIndex));
			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(ironPipeId.value), pipeItemsIron.shiftedIndex));
			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(goldenPipeId.value), pipeItemsGold.shiftedIndex));
			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(diamondPipeId.value), pipeItemsDiamond.shiftedIndex));
			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(obsidianPipeId.value), pipeItemsObsidian.shiftedIndex));
			ModLoader.RegisterBlock(new LegacyBlock(Integer
					.parseInt(cobblestonePipeId.value), pipeItemsCobblestone.shiftedIndex));

			ModLoader
					.RegisterTileEntity(LegacyTile.class,
							"net.buildcraft.src.buildcraft.transport.legacy.LegacyTile");
		}
		
		BuildCraftCore.mainConfiguration.save();

	}	
	
	private static Item createPipe (int id, Class <? extends Pipe> clas, String descr, Object r1, Object r2, Object r3) {
		Item res =  BlockGenericPipe.registerPipe (id, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		if (r1 != null && r2 != null && r3 != null) {						
			craftingmanager.addRecipe(new ItemStack(res, 8), new Object[] {
				"   ", "ABC", "   ", 
				Character.valueOf('A'), r1,
				Character.valueOf('B'), r2,
				Character.valueOf('C'), r3});
		} else if (r1 != null && r2 != null) {
			craftingmanager.addRecipe(new ItemStack(res, 1), new Object[] {
				"A ", "B ", 
				Character.valueOf('A'), r1,
				Character.valueOf('B'), r2});
		}
		
		return res;
	}

	public static void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		initialize ();
	}
}
