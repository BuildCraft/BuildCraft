package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemBuildCraftTexture;
//import net.minecraft.src.buildcraft.transport.BlockCobblestonePipe;
//import net.minecraft.src.buildcraft.transport.BlockDiamondPipe;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.Pipe;
//import net.minecraft.src.buildcraft.transport.BlockGoldenPipe;
//import net.minecraft.src.buildcraft.transport.BlockIronPipe;
//import net.minecraft.src.buildcraft.transport.BlockStonePipe;
//import net.minecraft.src.buildcraft.transport.BlockObsidianPipe;
//import net.minecraft.src.buildcraft.transport.BlockWoodenPipe;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
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
//import net.minecraft.src.buildcraft.transport.TileCobblestonePipe;
//import net.minecraft.src.buildcraft.transport.TileDiamondPipe;
//import net.minecraft.src.buildcraft.transport.TileGoldenPipe;
//import net.minecraft.src.buildcraft.transport.TileIronPipe;
//import net.minecraft.src.buildcraft.transport.TileObsidianPipe;
//import net.minecraft.src.buildcraft.transport.TileStonePipe;
//import net.minecraft.src.buildcraft.transport.TileWoodenPipe;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.Property;

public class BuildCraftTransport {
	
	private static boolean initialized = false;
	
//	public static BlockWoodenPipe woodenPipeBlock;
//	public static BlockStonePipe stonePipeBlock;
//	public static BlockIronPipe ironPipeBlock;
//	public static BlockGoldenPipe goldenPipeBlock;
//	public static BlockDiamondPipe diamondPipeBlock;
//	public static BlockObsidianPipe obsidianPipeBlock;
//	public static BlockCobblestonePipe cobblestonePipeBlock;
		
	public static BlockGenericPipe genericPipeBlock;
	
	public static int plainIronTexture;
	public static int [] diamondTextures = new int [6];
	
	public static boolean alwaysConnectPipes;
		
	public static Item pipeWaterproof;
	
	public static Item pipeItemsWood;
	public static Item pipeItemsStone;
	
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
			
//		Property woodenPipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("woodenPipe.id",
//						DefaultProps.WOODEN_PIPE_ID);
//		Property stonePipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("stonePipe.id",
//						DefaultProps.STONE_PIPE_ID);
//		Property ironPipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("ironPipe.id",
//						DefaultProps.IRON_PIPE_ID);
//		Property goldenPipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("goldenPipe.id",
//						DefaultProps.GOLDEN_PIPE_ID);
//		Property diamondPipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("diamondPipe.id",
//						DefaultProps.DIAMOND_PIPE_ID);
//		Property obsidianPipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("obsidianPipe.id",
//						DefaultProps.OBSIDIAN_PIPE_ID);
//		Property cobblestonePipeId = BuildCraftCore.mainConfiguration
//				.getOrCreateBlockIdProperty("cobblestonePipe.id",
//						DefaultProps.COBBLESTONE_PIPE_ID);
		
		Property alwaysConnect = BuildCraftCore.mainConfiguration
				.getOrCreateBooleanProperty("pipes.alwaysConnect",
						Configuration.GENERAL_PROPERTY,
						DefaultProps.PIPES_ALWAYS_CONNECT);
		alwaysConnect.comment = 
			"set to false to deactivate pipe connection rules, true by default";

//		Property exclusionList = BuildCraftCore.mainConfiguration
//				.getOrCreateProperty("woodenPipe.exclusion",
//						Configuration.BLOCK_PROPERTY, "");

//		BlockWoodenPipe.excludedBlocks = exclusionList.value.split(",");
//		
//		for (int j = 0; j < BlockWoodenPipe.excludedBlocks.length; ++j) {
//			BlockWoodenPipe.excludedBlocks[j] = BlockWoodenPipe.excludedBlocks[j]
//					.trim();
//		}
		
		BuildCraftCore.mainConfiguration.save();
		
//		CraftingManager craftingmanager = CraftingManager.getInstance();		
//		woodenPipeBlock = new BlockWoodenPipe(Integer.parseInt(woodenPipeId.value));
//		CoreProxy.addName(woodenPipeBlock.setBlockName("woodenPipe"), "Wooden Pipe");
//		ModLoader.RegisterBlock(woodenPipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(woodenPipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Block.planks,
//				Character.valueOf('G'), Block.glass});
//		
//		stonePipeBlock = new BlockStonePipe(Integer.parseInt(stonePipeId.value));
//		CoreProxy.addName(stonePipeBlock.setBlockName("stonePipe"), "Stone Pipe");
//		ModLoader.RegisterBlock(stonePipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(stonePipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Block.stone,
//				Character.valueOf('G'), Block.glass});
//		
//		ironPipeBlock = new BlockIronPipe(Integer.parseInt(ironPipeId.value));
//		CoreProxy.addName(ironPipeBlock.setBlockName("ironPipe"), "Iron Pipe");
//		ModLoader.RegisterBlock(ironPipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(ironPipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotIron,
//				Character.valueOf('G'), Block.glass});
//		
//		goldenPipeBlock = new BlockGoldenPipe(Integer.parseInt(goldenPipeId.value));
//		CoreProxy.addName(goldenPipeBlock.setBlockName("goldenPipe"), "Golden Pipe");
//		ModLoader.RegisterBlock(goldenPipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(goldenPipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotGold,
//				Character.valueOf('G'), Block.glass});
//		
//		diamondPipeBlock = new BlockDiamondPipe(Integer.parseInt(diamondPipeId.value));
//		CoreProxy.addName(diamondPipeBlock.setBlockName("diamondPipe"), "Diamond Pipe");
//		ModLoader.RegisterBlock(diamondPipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(diamondPipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Item.diamond,
//				Character.valueOf('G'), Block.glass});
//		
//		obsidianPipeBlock = new BlockObsidianPipe(Integer.parseInt(obsidianPipeId.value));
//		CoreProxy.addName(obsidianPipeBlock.setBlockName("obsidianPipe"), "Obsidian Pipe");
//		ModLoader.RegisterBlock(obsidianPipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(obsidianPipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Block.obsidian,
//				Character.valueOf('G'), Block.glass});
//		
//		cobblestonePipeBlock = new BlockCobblestonePipe(
//				Integer.parseInt(cobblestonePipeId.value));
//		CoreProxy.addName(cobblestonePipeBlock.setBlockName("cobblestonePipe"),
//				"Cobblestone Pipe");
//		ModLoader.RegisterBlock(cobblestonePipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(cobblestonePipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Block.cobblestone,
//				Character.valueOf('G'), Block.glass});
//		
//		ModLoader.RegisterTileEntity(TileWoodenPipe.class, "WoodenPipe");
//		ModLoader.RegisterTileEntity(TileStonePipe.class, "StonePipe");
//		ModLoader.RegisterTileEntity(TileIronPipe.class, "IronPipe");
//		ModLoader.RegisterTileEntity(TileGoldenPipe.class, "GoldenPipe");
//		ModLoader.RegisterTileEntity(TileDiamondPipe.class, "DiamondPipe");
//		ModLoader.RegisterTileEntity(TileObsidianPipe.class, "ObsidianPipe");
//		ModLoader.RegisterTileEntity(TileCobblestonePipe.class, "CobblestonePipe");		
		
		pipeWaterproof = new ItemBuildCraftTexture (DefaultProps.PIPE_WATERPROOF_ID).setIconIndex(2 * 16 + 1);
		pipeWaterproof.setItemName("pipeWaterproof");
		CoreProxy.addName(pipeWaterproof, "Pipe Waterproof");
		ModLoader.AddSmelting(Item.slimeBall.shiftedIndex, new ItemStack(
				pipeWaterproof, 32));
		
		genericPipeBlock = new BlockGenericPipe(166);
		
		pipeItemsWood = createPipe (4050, PipeItemsWood.class, "Wooden Transport Pipe", Block.planks, Block.glass, Block.planks);
		// cobblestone 4051
		pipeItemsStone = createPipe (4052, PipeItemsStone.class, "Stone Transport Pipe", Block.stone, Block.glass, Block.stone);
		// iron 4053
		// gold 4054
		// diamond 4055
		// obsidian 4056
		
		pipeLiquidsWood = createPipe (4057, PipeLiquidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood, null);
		pipeLiquidsCobblestone = createPipe (4058, PipeLiquidsCobblestone.class, "Cobblestone Waterproof Pipe", null, null, null);
		pipeLiquidsStone = createPipe (4059, PipeLiquidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone, null);		
		pipeLiquidsIron = createPipe (4060, PipeLiquidsIron.class, "Iron Waterproof Pipe", null, null, null);		
		pipeLiquidsGold = createPipe (4061, PipeLiquidsGold.class, "Golden Waterproof Pipe", null, null, null);			
		// diamond 4062		
		
		pipePowerWood = createPipe (4063, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone,  pipeItemsWood, null);		
		// cobblestone 4064
		pipePowerStone = createPipe (4065, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone, null);		
		// iron 4066
		pipePowerGold = createPipe(4067, PipePowerGold.class, "Golden Conductive Pipe", null, null, null);
		
		// diamond 4068
						
//				Integer.parseInt(cobblestonePipeId.value));
//		CoreProxy.addName(cobblestonePipeBlock.setBlockName("cobblestonePipe"),
//				"Cobblestone Pipe");
//		ModLoader.RegisterBlock(cobblestonePipeBlock);		
//		craftingmanager.addRecipe(new ItemStack(cobblestonePipeBlock, 8), new Object[] {
//				"   ", "PGP", "   ", Character.valueOf('P'), Block.cobblestone,
//				Character.valueOf('G'), Block.glass});
		
		plainIronTexture = 1 * 16 + 3;
		
		for (int j = 0; j < 6; ++j) {
			diamondTextures [j] = 1 * 16 + 6 + j;
		}				
		
		alwaysConnectPipes = Boolean.parseBoolean(alwaysConnect.value);
		
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
