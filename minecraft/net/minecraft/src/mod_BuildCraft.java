package net.minecraft.src;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.BlockMachine;
import net.minecraft.src.buildcraft.BlockMiningWell;
import net.minecraft.src.buildcraft.BlockPipe;
import net.minecraft.src.buildcraft.BlockPlainPipe;
import net.minecraft.src.buildcraft.BlockRooter;
import net.minecraft.src.buildcraft.EntityPassiveItem;
import net.minecraft.src.buildcraft.ITickListener;
import net.minecraft.src.buildcraft.ItemWoodGear;
import net.minecraft.src.buildcraft.TileMachine;
import net.minecraft.src.buildcraft.TileMiningWell;
import net.minecraft.src.buildcraft.TilePipe;
import net.minecraft.src.buildcraft.TileRooter;

public class mod_BuildCraft extends BaseMod {	

	private static mod_BuildCraft instance;

	public final ItemWoodGear woodGearItem;
	
	public final BlockMachine machineBlock;
	public final BlockPipe pipeBlock;
	public final BlockRooter rooterBlock;
	public final BlockMiningWell miningWellBlock;
	public final BlockPlainPipe plainPipeBlock;
	
	private class TickContainer {
		ITickListener listener;
		int pace;
	}
	
	public HashMap <ITickListener, TickContainer> tickListeners = new HashMap <ITickListener, TickContainer> ();
	
	public LinkedList<TickContainer> tickListenersScheduledForAddition = new LinkedList<TickContainer>(); 
	
	public LinkedList <ITickListener> tickListenersScheduledForRemoval = new LinkedList <ITickListener> (); 
	
	public mod_BuildCraft () {		
		instance = this;
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		woodGearItem = (ItemWoodGear) (new ItemWoodGear(ModLoader.getUniqueEntityId())).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/buildcraft_gui/wood_gear.png")).setItemName(
				"woodGearItem");
		ModLoader.AddName(woodGearItem, "Wood Gear");
		
		machineBlock = new BlockMachine (getFirstFreeBlock ());
		ModLoader.RegisterBlock(machineBlock);
		craftingmanager.addRecipe(new ItemStack(machineBlock), new Object[] {
			"##", Character.valueOf('#'), Block.dirt });
		
		pipeBlock = new BlockPipe (getFirstFreeBlock ());
		ModLoader.RegisterBlock(pipeBlock);
		ModLoader.AddName(woodGearItem, "Wood Pipe");
		craftingmanager.addRecipe(new ItemStack(pipeBlock, 30), new Object[] {
			"#", "#", Character.valueOf('#'), Block.dirt });
		
		rooterBlock = new BlockRooter (getFirstFreeBlock ());
		ModLoader.RegisterBlock(rooterBlock);
		craftingmanager.addRecipe(new ItemStack(rooterBlock, 1), new Object[] {
			"# ", " #", Character.valueOf('#'), Block.dirt });
		
		miningWellBlock = new BlockMiningWell (getFirstFreeBlock ());
		ModLoader.RegisterBlock(miningWellBlock);
		craftingmanager.addRecipe(new ItemStack(miningWellBlock, 1), new Object[] {
			"##", "##", Character.valueOf('#'), Block.dirt });
		
		plainPipeBlock = new BlockPlainPipe (getFirstFreeBlock ());
		ModLoader.RegisterBlock(plainPipeBlock);

		ModLoader.SetInGameHook(this, true, false);		
		
		ModLoader.RegisterTileEntity(TileMachine.class, "Machine");
		ModLoader.RegisterTileEntity(TilePipe.class, "Pipe");
		ModLoader.RegisterTileEntity(TileRooter.class, "Rooter");
		ModLoader.RegisterTileEntity(TileMiningWell.class, "MiningWell");	
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
    }
    
    private boolean isPipeConnected (int id) {
		return id == pipeBlock.blockID || id == machineBlock.blockID
				|| id == rooterBlock.blockID || id == Block.crate.blockID
				|| id == miningWellBlock.blockID;
    }
    
	public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
    	if (block.getRenderType() == pipeBlock.modelID) {    		
    		block.setBlockBounds(0.3F, 0.3F, 0.3F, 0.7F, 0.7F, 0.7F);
    		renderblocks.renderStandardBlock(block, i, j, k);
    		
    		if (isPipeConnected (iblockaccess.getBlockId(i - 1, j, k))) {
    			block.setBlockBounds(0.0F, 0.3F, 0.3F, 0.3F, 0.7F, 0.7F);
        		renderblocks.renderStandardBlock(block, i, j, k);
    		}
    		
    		if (isPipeConnected (iblockaccess.getBlockId(i + 1, j, k))) {
    			block.setBlockBounds(0.7F, 0.3F, 0.3F, 1.0F, 0.7F, 0.7F);
        		renderblocks.renderStandardBlock(block, i, j, k);
    		}
    		
    		if (isPipeConnected (iblockaccess.getBlockId(i, j - 1, k))) {
    			block.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.3F, 0.7F);
        		renderblocks.renderStandardBlock(block, i, j, k);
    		}
    		
    		if (isPipeConnected (iblockaccess.getBlockId(i, j + 1, k))) {
    			block.setBlockBounds(0.3F, 0.7F, 0.3F, 0.7F, 1.0F, 0.7F);
        		renderblocks.renderStandardBlock(block, i, j, k);
    		}
    		
    		if (isPipeConnected (iblockaccess.getBlockId(i, j, k - 1))) {
    			block.setBlockBounds(0.3F, 0.3F, 0.0F, 0.7F, 0.7F, 0.3F);
        		renderblocks.renderStandardBlock(block, i, j, k);
    		}
    		
    		if (isPipeConnected (iblockaccess.getBlockId(i, j, k + 1))) {
    			block.setBlockBounds(0.3F, 0.3F, 0.7F, 0.7F, 0.7F, 1.0F);
        		renderblocks.renderStandardBlock(block, i, j, k);
    		}
    		
    		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    		
    		return true;
    	} 
    	
        return false;
    }
    
    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j)
    {
		if (block.getRenderType() == pipeBlock.modelID) {    
    		Tessellator tessellator = Tessellator.instance;    		

    		block.setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 1.0F, 0.7F);
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
