package net.minecraft.src;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.BlockAutoWorkbench;
import net.minecraft.src.buildcraft.BlockCheat;
import net.minecraft.src.buildcraft.BlockDiamondPipe;
import net.minecraft.src.buildcraft.BlockFrame;
import net.minecraft.src.buildcraft.BlockGoldenPipe;
import net.minecraft.src.buildcraft.BlockIronPipe;
import net.minecraft.src.buildcraft.BlockMachine;
import net.minecraft.src.buildcraft.BlockMiningWell;
import net.minecraft.src.buildcraft.BlockPlainPipe;
import net.minecraft.src.buildcraft.BlockStonePipe;
import net.minecraft.src.buildcraft.BlockWoodenPipe;
import net.minecraft.src.buildcraft.EntityBlock;
import net.minecraft.src.buildcraft.EntityMechanicalArm;
import net.minecraft.src.buildcraft.EntityPassiveItem;
import net.minecraft.src.buildcraft.ITickListener;
import net.minecraft.src.buildcraft.Orientations;
import net.minecraft.src.buildcraft.RenderEntityBlock;
import net.minecraft.src.buildcraft.TileAutoWorkbench;
import net.minecraft.src.buildcraft.TileDiamondPipe;
import net.minecraft.src.buildcraft.TileGoldenPipe;
import net.minecraft.src.buildcraft.TileIronPipe;
import net.minecraft.src.buildcraft.TileMachine;
import net.minecraft.src.buildcraft.TileMiningWell;
import net.minecraft.src.buildcraft.TileStonePipe;
import net.minecraft.src.buildcraft.TileWoodenPipe;
import net.minecraft.src.buildcraft.Utils;
import net.minecraft.src.buildcraft.RenderVoid;

public class mod_BuildCraft extends BaseMod {	

	private static mod_BuildCraft instance;

	public final Item woodenGearItem;
	public final Item stoneGearItem;
	public final Item ironGearItem;
	public final Item goldGearItem;
	public final Item diamondGearItem;
	
	public final BlockMachine machineBlock;
	
	public final BlockMiningWell miningWellBlock;
	
	public final BlockWoodenPipe woodenPipeBlock;
	public final BlockStonePipe stonePipeBlock;
	public final BlockIronPipe ironPipeBlock;
	public final BlockGoldenPipe goldenPipeBlock;
	public final BlockDiamondPipe diamondPipeBlock;
	public final BlockPlainPipe plainPipeBlock;
	public final BlockAutoWorkbench autoWorkbenchBlock;
	public final BlockFrame frameBlock;
	
	public final BlockCheat cheatBlock;
	
	public final int pipeModel;
	
	public final int plainIronTexture;
	public final int [] diamondTextures = new int [6];
	public final int drillTexture;
	
	private class TickContainer {
		ITickListener listener;
		int pace;
	}
	
	public HashMap <ITickListener, TickContainer> tickListeners = new HashMap <ITickListener, TickContainer> ();
	
	public LinkedList<TickContainer> tickListenersScheduledForAddition = new LinkedList<TickContainer>(); 
	
	public LinkedList <ITickListener> tickListenersScheduledForRemoval = new LinkedList <ITickListener> (); 
	
	public mod_BuildCraft () {		
		instance = this;
		
		pipeModel = ModLoader.getUniqueBlockModelID(this, true);
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		woodenGearItem = (new Item(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/wooden_gear.png")).setItemName(
				"woodenGearItem");				
		craftingmanager.addRecipe(new ItemStack(woodenGearItem), new Object[] {
		" S ", "S S", " S ", Character.valueOf('S'), Item.stick});
		ModLoader.AddName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new Item(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/stone_gear.png")).setItemName(
				"stoneGearItem");
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
				Character.valueOf('G'), woodenGearItem });
		ModLoader.AddName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new Item(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/iron_gear.png")).setItemName(
				"ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		ModLoader.AddName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new Item(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/golden_gear.png")).setItemName(
				"goldGearItem");
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
				Character.valueOf('G'), ironGearItem });
		ModLoader.AddName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new Item(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/diamond_gear.png")).setItemName(
				"diamondGearItem");
		craftingmanager.addRecipe(new ItemStack(diamondGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.diamond,
				Character.valueOf('G'), goldGearItem });
		ModLoader.AddName(diamondGearItem, "Diamond Gear");
		
		woodenPipeBlock = new BlockWoodenPipe(getFirstFreeBlock());
		ModLoader.AddName(woodenPipeBlock.setBlockName("woodenPipe"), "Wooden Pipe");
		ModLoader.RegisterBlock(woodenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(woodenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.planks,
				Character.valueOf('G'), Block.glass});
		
		stonePipeBlock = new BlockStonePipe(getFirstFreeBlock());
		ModLoader.AddName(stonePipeBlock.setBlockName("stonePipe"), "Stone Pipe");
		ModLoader.RegisterBlock(stonePipeBlock);		
		craftingmanager.addRecipe(new ItemStack(stonePipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.cobblestone,
				Character.valueOf('G'), Block.glass});
		
		ironPipeBlock = new BlockIronPipe(getFirstFreeBlock());
		ModLoader.AddName(ironPipeBlock.setBlockName("ironPipe"), "Iron Pipe");
		ModLoader.RegisterBlock(ironPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(ironPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotIron,
				Character.valueOf('G'), Block.glass});
		
		goldenPipeBlock = new BlockGoldenPipe(getFirstFreeBlock());
		ModLoader.AddName(goldenPipeBlock.setBlockName("goldenPipe"), "Golden Pipe");
		ModLoader.RegisterBlock(goldenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(goldenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotGold,
				Character.valueOf('G'), Block.glass});
		
		diamondPipeBlock = new BlockDiamondPipe(getFirstFreeBlock());
		ModLoader.AddName(diamondPipeBlock.setBlockName("diamondPipe"), "Diamond Pipe");
		ModLoader.RegisterBlock(diamondPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(diamondPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.diamond,
				Character.valueOf('G'), Block.glass});
		
		
		miningWellBlock = new BlockMiningWell (getFirstFreeBlock ());
		ModLoader.RegisterBlock(miningWellBlock);
		ModLoader.AddName(miningWellBlock.setBlockName("miningWellBlock"), "Mining Well");
		craftingmanager.addRecipe(new ItemStack(miningWellBlock, 1), new Object[] {
			"ipi", "igi", "iPi", Character.valueOf('p'), ironPipeBlock,
			Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'),
			ironGearItem, Character.valueOf('P'),
			Item.pickaxeSteel });	
		
		plainPipeBlock = new BlockPlainPipe (getFirstFreeBlock ());
		ModLoader.RegisterBlock(plainPipeBlock);
		ModLoader.AddName(plainPipeBlock.setBlockName("plainPipeBlock"), "Mining Pipe");
		
		autoWorkbenchBlock = new BlockAutoWorkbench (getFirstFreeBlock());
		ModLoader.RegisterBlock(autoWorkbenchBlock);
		craftingmanager.addRecipe(
				new ItemStack(autoWorkbenchBlock),
				new Object[] { " g ", "gwg", " g ", Character.valueOf('w'),
						Block.workbench, Character.valueOf('g'),
						mod_BuildCraft.getInstance().woodenGearItem });
		ModLoader.AddName(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"),
				"Automatic Crafting Table");
				
		frameBlock = new BlockFrame (getFirstFreeBlock ());
		ModLoader.RegisterBlock(frameBlock);
		
		machineBlock = new BlockMachine (getFirstFreeBlock ());
		ModLoader.RegisterBlock(machineBlock);
		craftingmanager.addRecipe(
				new ItemStack(machineBlock),
				new Object[] { "ipi", "gdg", "dDd", 
					Character.valueOf('i'),	mod_BuildCraft.getInstance().ironGearItem,
					Character.valueOf('p'),	mod_BuildCraft.getInstance().diamondPipeBlock,
					Character.valueOf('g'),	mod_BuildCraft.getInstance().goldGearItem,
					Character.valueOf('d'),	mod_BuildCraft.getInstance().diamondGearItem,
					Character.valueOf('D'),	Item.pickaxeDiamond,
					});
		ModLoader.AddName(machineBlock.setBlockName("machineBlock"),
		"Quarry");
		
		cheatBlock = new BlockCheat (getFirstFreeBlock());
		ModLoader.RegisterBlock(cheatBlock);
		craftingmanager.addRecipe(new ItemStack(cheatBlock, 1), new Object[] {
			"# ", "  ", Character.valueOf('#'), Block.dirt });
		
		ModLoader.SetInGameHook(this, true, false);		
		
		ModLoader.RegisterTileEntity(TileMachine.class, "Machine");		
		ModLoader.RegisterTileEntity(TileMiningWell.class, "MiningWell");
		ModLoader.RegisterTileEntity(TileWoodenPipe.class, "WoodenPipe");
		ModLoader.RegisterTileEntity(TileStonePipe.class, "StonePipe");
		ModLoader.RegisterTileEntity(TileIronPipe.class, "IronPipe");
		ModLoader.RegisterTileEntity(TileGoldenPipe.class, "GoldenPipe");
		ModLoader.RegisterTileEntity(TileDiamondPipe.class, "DiamondPipe");
		ModLoader.RegisterTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
			
		
		plainIronTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/plain_iron_pipe.png");
		
		for (int j = 0; j < 6; ++j) {
			diamondTextures [j] = ModLoader.addOverride("/terrain.png",
					"/buildcraft_gui/diamond_pipe_" + j + ".png");
		}				
		
		drillTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/drill.png");
		
	}
		
	private int getFirstFreeBlock() {
		for (int i = Block.blocksList.length - 1; i >= 0; --i) {
			if (Block.blocksList [i] == null) {
				return i;
			}
		}
		
		return -1;
	}

	public static mod_BuildCraft getInstance () {
		return instance;
	}
	
	
	@Override
	public String Version() {
		return "1.5_01.3";
	}
	    
    long lastTick = 0;
    
    public void registerTicksListener (ITickListener listener, int pace) {
    	//  TODO: move registers on tiles and use the date to select when to
    	//  make the call.
    	TickContainer container = new TickContainer();
    	container.listener = listener;
    	container.pace = pace;    	    	
    	tickListenersScheduledForAddition.add(container);
    }
    
    public void OnTickInGame(Minecraft minecraft)
    {    
    	for (ITickListener listener : tickListenersScheduledForRemoval) {    	    		
    		if (tickListeners.containsKey(listener)) {
    			tickListeners.remove(listener);
    		}
    	}
    	
    	for (TickContainer container : tickListenersScheduledForAddition) {    		    		    	
    		tickListeners.put (container.listener, container);    		
    	}
    	
    	tickListenersScheduledForAddition.clear ();
    	tickListenersScheduledForRemoval.clear ();
    	
    	if (minecraft.theWorld.getWorldTime() != lastTick) {    		    		
    		lastTick = minecraft.theWorld.getWorldTime();
    		
    		for (TickContainer container : tickListeners.values()) {
    			if (lastTick % container.pace == 0) {
    				container.listener.tick(minecraft);	
    			}				
			}    		
    	}
    	
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	map.put (EntityPassiveItem.class, new RenderItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());
    	map.put (EntityMechanicalArm.class, new RenderVoid());
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
    		
    		if (Utils.isPipeConnected (iblockaccess, i - 1, j, k, block.blockID)) {
    			if (block == ironPipeBlock && metadata != Orientations.XNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.XNeg.ordinal()];
    			}
    			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess, i + 1, j, k, block.blockID)) {
    			if (block == ironPipeBlock && metadata != Orientations.XPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.XPos.ordinal()];
    			}
    			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess, i, j - 1, k, block.blockID)) {
    			if (block == ironPipeBlock && metadata != Orientations.YNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.YNeg.ordinal()];
    			}
    			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess, i, j + 1, k, block.blockID)) {
    			if (block == ironPipeBlock && metadata != Orientations.YPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.YPos.ordinal()];
    			}
    			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess, i, j, k - 1, block.blockID)) {
    			if (block == ironPipeBlock && metadata != Orientations.ZNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			} else if (block == diamondPipeBlock) {
    				block.blockIndexInTexture = diamondTextures [Orientations.ZNeg.ordinal()];
    			}
    			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess, i, j, k + 1, block.blockID)) {
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

	public void unregisterTicksListener(ITickListener tilePipe) {
		tickListenersScheduledForRemoval.add(tilePipe);
	}
}
