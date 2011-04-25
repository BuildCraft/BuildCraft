package net.minecraft.src;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.BlockCheat;
import net.minecraft.src.buildcraft.BlockExtractor;
import net.minecraft.src.buildcraft.BlockIronPipe;
import net.minecraft.src.buildcraft.BlockMachine;
import net.minecraft.src.buildcraft.BlockMiningWell;
import net.minecraft.src.buildcraft.BlockPipe;
import net.minecraft.src.buildcraft.BlockPlainPipe;
import net.minecraft.src.buildcraft.BlockFilter;
import net.minecraft.src.buildcraft.BlockWoodenPipe;
import net.minecraft.src.buildcraft.EntityDigger;
import net.minecraft.src.buildcraft.EntityPassiveItem;
import net.minecraft.src.buildcraft.ITickListener;
import net.minecraft.src.buildcraft.Orientations;
import net.minecraft.src.buildcraft.RenderEntityDigger;
import net.minecraft.src.buildcraft.TileExtractor;
import net.minecraft.src.buildcraft.TileIronPipe;
import net.minecraft.src.buildcraft.TileMachine;
import net.minecraft.src.buildcraft.TileMiningWell;
import net.minecraft.src.buildcraft.TilePipe;
import net.minecraft.src.buildcraft.TileRooter;
import net.minecraft.src.buildcraft.Utils;

public class mod_BuildCraft extends BaseMod {	

	private static mod_BuildCraft instance;

	public final Item woodenGearItem;
	public final Item ironGearItem;
	
	public final BlockMachine machineBlock;
	public final BlockWoodenPipe woodenPipeBlock;
	public final BlockIronPipe ironPipeBlock;
	public final BlockFilter filterBlock;
	public final BlockMiningWell miningWellBlock;
	public final BlockPlainPipe plainPipeBlock;
	public final BlockExtractor extractorBlock;
	public final BlockCheat cheatBlock;
	
	public final int pipeModel;
	public final int plainIronTexture;
	
	
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
		
		ironGearItem = (new Item(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/iron_gear.png")).setItemName(
				"ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), woodenGearItem });
		ModLoader.AddName(ironGearItem, "Iron Gear");
		
		machineBlock = new BlockMachine (getFirstFreeBlock ());
		ModLoader.RegisterBlock(machineBlock);
		craftingmanager.addRecipe(new ItemStack(machineBlock), new Object[] {
			"ss", "  ", Character.valueOf('s'), Block.dirt });
		
		woodenPipeBlock = new BlockWoodenPipe(getFirstFreeBlock());
		ModLoader.AddName(woodenPipeBlock.setBlockName("woodenPipe"), "Wooden Pipe");
		ModLoader.RegisterBlock(woodenPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(woodenPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Block.planks,
				Character.valueOf('G'), Block.glass});
		
		ironPipeBlock = new BlockIronPipe(getFirstFreeBlock());
		ModLoader.AddName(ironPipeBlock.setBlockName("ironPipe"), "Iron Pipe");
		ModLoader.RegisterBlock(ironPipeBlock);		
		craftingmanager.addRecipe(new ItemStack(ironPipeBlock, 8), new Object[] {
				"   ", "PGP", "   ", Character.valueOf('P'), Item.ingotIron,
				Character.valueOf('G'), Block.glass});
		
		filterBlock = new BlockFilter (getFirstFreeBlock ());
		ModLoader.RegisterBlock(filterBlock);
		ModLoader.AddName(filterBlock.setBlockName("filerBlock"), "Filter");
		craftingmanager.addRecipe(new ItemStack(filterBlock, 1), new Object[] {
				"ppp", "igi", "ppp", Character.valueOf('p'), ironPipeBlock,
				Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'),
				ironGearItem });		
		
		miningWellBlock = new BlockMiningWell (getFirstFreeBlock ());
		ModLoader.RegisterBlock(miningWellBlock);
		ModLoader.AddName(miningWellBlock.setBlockName("miningWellBlock"), "Iron Mining Well");
		craftingmanager.addRecipe(new ItemStack(miningWellBlock, 1), new Object[] {
			"ipi", "igi", "iPi", Character.valueOf('p'), ironPipeBlock,
			Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'),
			ironGearItem, Character.valueOf('P'),
			Item.pickaxeSteel });	
		
		plainPipeBlock = new BlockPlainPipe (getFirstFreeBlock ());
		ModLoader.RegisterBlock(plainPipeBlock);
		ModLoader.AddName(plainPipeBlock.setBlockName("plainPipeBlock"), "Mining Pipe");
		
		extractorBlock = new BlockExtractor(getFirstFreeBlock ());
		ModLoader.RegisterBlock(extractorBlock);
		ModLoader.AddName(extractorBlock.setBlockName("extractor"), "Chest Extractor");
		craftingmanager.addRecipe(new ItemStack(extractorBlock, 1), new Object[] {
			"  ", "pg ", "  ", Character.valueOf('p'), woodenPipeBlock,
			Character.valueOf('g'), woodenGearItem });	
		
		cheatBlock = new BlockCheat (getFirstFreeBlock());
		ModLoader.RegisterBlock(cheatBlock);
		craftingmanager.addRecipe(new ItemStack(cheatBlock, 1), new Object[] {
			"# ", "  ", Character.valueOf('#'), Block.dirt });
		
		ModLoader.SetInGameHook(this, true, false);		
		
		ModLoader.RegisterTileEntity(TileMachine.class, "Machine");
		ModLoader.RegisterTileEntity(TilePipe.class, "Pipe");
		ModLoader.RegisterTileEntity(TileRooter.class, "Rooter");
		ModLoader.RegisterTileEntity(TileMiningWell.class, "MiningWell");
		ModLoader.RegisterTileEntity(TileExtractor.class, "Extractor");
		ModLoader.RegisterTileEntity(TileIronPipe.class, "IronPipe");		
		
		plainIronTexture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/plain_iron_pipe.png");
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
		return "0.1";
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
    	map.put (EntityDigger.class, new RenderEntityDigger());
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
    		
    		if (Utils.isPipeConnected (iblockaccess.getBlockId(i - 1, j, k))) {
    			if (block == ironPipeBlock && metadata != Orientations.XNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			}
    			block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess.getBlockId(i + 1, j, k))) {
    			if (block == ironPipeBlock && metadata != Orientations.XPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			}
    			block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess.getBlockId(i, j - 1, k))) {
    			if (block == ironPipeBlock && metadata != Orientations.YNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			}
    			block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess.getBlockId(i, j + 1, k))) {
    			if (block == ironPipeBlock && metadata != Orientations.YPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			}
    			block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F, maxSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess.getBlockId(i, j, k - 1))) {
    			if (block == ironPipeBlock && metadata != Orientations.ZNeg.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
    			}
    			block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize, minSize);
        		renderblocks.renderStandardBlock(block, i, j, k);
        		block.blockIndexInTexture = initialTexture;
    		}
    		
    		if (Utils.isPipeConnected (iblockaccess.getBlockId(i, j, k + 1))) {
    			if (block == ironPipeBlock && metadata != Orientations.ZPos.ordinal()) {
    				block.blockIndexInTexture = plainIronTexture;
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
