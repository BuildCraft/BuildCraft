package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.BlockDiamondPipe;
import net.minecraft.src.buildcraft.transport.BlockGoldenPipe;
import net.minecraft.src.buildcraft.transport.BlockIronPipe;
import net.minecraft.src.buildcraft.transport.BlockStonePipe;
import net.minecraft.src.buildcraft.transport.BlockWoodenPipe;
import net.minecraft.src.buildcraft.transport.TileDiamondPipe;
import net.minecraft.src.buildcraft.transport.TileGoldenPipe;
import net.minecraft.src.buildcraft.transport.TileIronPipe;
import net.minecraft.src.buildcraft.transport.TileStonePipe;
import net.minecraft.src.buildcraft.transport.TileWoodenPipe;

public class BuildCraftTransport {
	
	private static boolean initialized = false;
	
	public static BlockWoodenPipe woodenPipeBlock;
	public static BlockStonePipe stonePipeBlock;
	public static BlockIronPipe ironPipeBlock;
	public static BlockGoldenPipe goldenPipeBlock;
	public static BlockDiamondPipe diamondPipeBlock;
	
	public static int pipeModel;
		
	public static int plainIronTexture;
	public static int [] diamondTextures = new int [6];
	
	public static void initialize () {
		if (initialized) {
			return;
		}
		
		initialized = true;
		
		mod_BuildCraftCore.initialize();						
			
		CraftingManager craftingmanager = CraftingManager.getInstance();		
		woodenPipeBlock = new BlockWoodenPipe(Utils.getSafeBlockId(
				"woodenPipe.blockId", 145));
		CoreProxy.addName(woodenPipeBlock.setBlockName("woodenPipe"), "Wooden Pipe");
		ModLoader.RegisterBlock(woodenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(woodenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.planks,
				Character.valueOf('G'), Block.glass});
		
		stonePipeBlock = new BlockStonePipe(Utils.getSafeBlockId(
				"stonePipe.blockId", 146));
		CoreProxy.addName(stonePipeBlock.setBlockName("stonePipe"), "Stone Pipe");
		ModLoader.RegisterBlock(stonePipeBlock);		
		craftingmanager.addRecipe(new ItemStack(stonePipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.cobblestone,
				Character.valueOf('G'), Block.glass});
		
		ironPipeBlock = new BlockIronPipe(Utils.getSafeBlockId(
				"ironPipe.blockId", 147));
		CoreProxy.addName(ironPipeBlock.setBlockName("ironPipe"), "Iron Pipe");
		ModLoader.RegisterBlock(ironPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(ironPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotIron,
				Character.valueOf('G'), Block.glass});
		
		goldenPipeBlock = new BlockGoldenPipe(Utils.getSafeBlockId(
				"goldenPipe.blockId", 148));
		CoreProxy.addName(goldenPipeBlock.setBlockName("goldenPipe"), "Golden Pipe");
		ModLoader.RegisterBlock(goldenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(goldenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotGold,
				Character.valueOf('G'), Block.glass});
		
		diamondPipeBlock = new BlockDiamondPipe(Utils.getSafeBlockId(
				"diamondPipe.blockId", 149));
		CoreProxy.addName(diamondPipeBlock.setBlockName("diamondPipe"), "Diamond Pipe");
		ModLoader.RegisterBlock(diamondPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(diamondPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.diamond,
				Character.valueOf('G'), Block.glass});
		
		ModLoader.RegisterTileEntity(TileWoodenPipe.class, "WoodenPipe");
		ModLoader.RegisterTileEntity(TileStonePipe.class, "StonePipe");
		ModLoader.RegisterTileEntity(TileIronPipe.class, "IronPipe");
		ModLoader.RegisterTileEntity(TileGoldenPipe.class, "GoldenPipe");
		ModLoader.RegisterTileEntity(TileDiamondPipe.class, "DiamondPipe");
		
		plainIronTexture = ModLoader.addOverride("/terrain.png",
		"/net/minecraft/src/buildcraft/transport/gui/plain_iron_pipe.png");
		
		for (int j = 0; j < 6; ++j) {
			diamondTextures [j] = ModLoader.addOverride("/terrain.png",
					"/net/minecraft/src/buildcraft/transport/gui/diamond_pipe_" + j + ".png");
		}				
		
		Utils.saveProperties();

	}
	
	public static void initializeModel (BaseMod mod) {
		pipeModel = ModLoader.getUniqueBlockModelID(mod, true);
	}
	

	public static void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		initialize ();
	}
}
