package net.minecraft.src;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.buildcraft.core.IPipeConnection;
import net.minecraft.src.buildcraft.core.Orientations;
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

public class mod_BuildCraftTransport extends BaseMod {
	
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
		woodenPipeBlock = new BlockWoodenPipe(Integer.parseInt(Utils
				.getProperty("woodenPipe.blockId", "255")));
		ModLoader.AddName(woodenPipeBlock.setBlockName("woodenPipe"), "Wooden Pipe");
		ModLoader.RegisterBlock(woodenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(woodenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.planks,
				Character.valueOf('G'), Block.glass});
		
		stonePipeBlock = new BlockStonePipe(Integer.parseInt(Utils.getProperty(
				"stonePipe.blockId", "254")));
		ModLoader.AddName(stonePipeBlock.setBlockName("stonePipe"), "Stone Pipe");
		ModLoader.RegisterBlock(stonePipeBlock);		
		craftingmanager.addRecipe(new ItemStack(stonePipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.cobblestone,
				Character.valueOf('G'), Block.glass});
		
		ironPipeBlock = new BlockIronPipe(Integer.parseInt(Utils.getProperty(
				"ironPipe.blockId", "253")));
		ModLoader.AddName(ironPipeBlock.setBlockName("ironPipe"), "Iron Pipe");
		ModLoader.RegisterBlock(ironPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(ironPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotIron,
				Character.valueOf('G'), Block.glass});
		
		goldenPipeBlock = new BlockGoldenPipe(Integer.parseInt(Utils
				.getProperty("goldenPipe.blockId", "252")));
		ModLoader.AddName(goldenPipeBlock.setBlockName("goldenPipe"), "Golden Pipe");
		ModLoader.RegisterBlock(goldenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(goldenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotGold,
				Character.valueOf('G'), Block.glass});
		
		diamondPipeBlock = new BlockDiamondPipe(Integer.parseInt(Utils
				.getProperty("diamondPipe.blockId", "251")));
		ModLoader.AddName(diamondPipeBlock.setBlockName("diamondPipe"), "Diamond Pipe");
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
	

	public void ModsLoaded () {
		super.ModsLoaded();
		
		initialize ();
		
		pipeModel = ModLoader.getUniqueBlockModelID(this, true);
	}
	
		
	@Override
	public String Version() {
		return "1.5_01.4";
	}
	
	public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
		
    	if (block.getRenderType() == pipeModel) {
    		float minSize = Utils.pipeMinSize;
    		float maxSize = Utils.pipeMaxSize;
    		int initialTexture = block.blockIndexInTexture;
    		
    		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
    		renderblocks.renderStandardBlock(block, i, j, k);
    		
    		int metadata = iblockaccess.getBlockMetadata(i, j, k);
    		
    		IPipeConnection connect = (IPipeConnection) block;
    		
    		if (connect.isPipeConnected (iblockaccess, i - 1, j, k)) {
    			if (block == ironPipeBlock && metadata != Orientations.XNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.XNeg.ordinal()];
    			}
    			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (connect.isPipeConnected (iblockaccess, i + 1, j, k)) {
    			if (block == ironPipeBlock && metadata != Orientations.XPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.XPos.ordinal()];
    			}
    			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (connect.isPipeConnected (iblockaccess, i, j - 1, k)) {
    			if (block == ironPipeBlock && metadata != Orientations.YNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.YNeg.ordinal()];
    			}
    			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (connect.isPipeConnected (iblockaccess, i, j + 1, k)) {
    			if (block == ironPipeBlock && metadata != Orientations.YPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.YPos.ordinal()];
    			}
    			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (connect.isPipeConnected (iblockaccess, i, j, k - 1)) {
    			if (block == ironPipeBlock && metadata != Orientations.ZNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.ZNeg.ordinal()];
    			}
    			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (connect.isPipeConnected (iblockaccess, i, j, k + 1)) {
    			if (block == ironPipeBlock && metadata != Orientations.ZPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.ZPos.ordinal()];
    			}
    			block.setBlockBounds(minSize, minSize, maxSize, maxSize, maxSize, 1.0F);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    		
    		return true;
    	} 
    	
        return false;
    }
    
    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j)
    {
		if (block.getRenderType() == pipeModel) {			
    		Tessellator tessellator = Tessellator.instance;    		

    		block.setBlockBounds(Utils.pipeMinSize, 0.0F, Utils.pipeMinSize, Utils.pipeMaxSize, 1.0F, Utils.pipeMaxSize);
            block.setBlockBoundsForItemRender();
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1F, 0.0F);
            renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1F);
            renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1F, 0.0F, 0.0F);
            renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, i));
            tessellator.draw();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    	}
    }
	    

}
