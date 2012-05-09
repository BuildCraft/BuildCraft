/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BuildCraftAPI {

	public static final int BUCKET_VOLUME = 1000;
	public static final int LAST_ORIGINAL_BLOCK = 122;
	public static final int LAST_ORIGINAL_ITEM = 126;
	
	// BuildCraft additional block and item data
	
	public static boolean [] softBlocks = new boolean [Block.blocksList.length];
	public static BptBlock[] blockBptProps = new BptBlock[Block.blocksList.length];
	
	// Other BuildCraft global data
	
	public static LinkedList <LiquidData> liquids = new LinkedList <LiquidData> ();	
	public static HashMap<Integer, IronEngineFuel> ironEngineFuel = new HashMap<Integer, IronEngineFuel>();		
	public static Trigger [] triggers = new Trigger [1024];
	public static Action [] actions = new Action [1024];
	
	private static EntityPlayer buildCraftPlayer;
	private static LinkedList <RefineryRecipe> refineryRecipe = new LinkedList <RefineryRecipe> ();
	private static LinkedList <ITriggerProvider> triggerProviders = new LinkedList <ITriggerProvider> ();
	private static LinkedList <IActionProvider> actionProviders = new LinkedList <IActionProvider> ();

	public static int getLiquidForFilledItem(ItemStack filledItem) {
		if (filledItem == null) {
			return 0;
		}
		
		for (LiquidData d : liquids) {
			if (d.filled.itemID == filledItem.itemID
				&& d.filled.getItemDamage() == filledItem.getItemDamage()) {
				return d.liquidId;
			}
		}
		
		return 0;
	}  

	public static ItemStack getFilledItemForLiquid(int liquidId) {
		for (LiquidData d : liquids) {
			if (d.liquidId == liquidId) {
				return d.filled.copy();
			}
		}
		
		return null;
	}
	
	public static boolean isLiquid (int blockId) {
		if (blockId == 0) {
			return false;
		}
		
		for (LiquidData d : liquids) {
			if (d.liquidId == blockId || d.movingLiquidId == blockId) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Return true if the block given in parameter is pass through (e.g. air,
	 * water...)
	 */
	public static boolean softBlock (int blockId) {
		return blockId == 0 
				|| softBlocks [blockId]
				|| Block.blocksList [blockId] == null;
	}

	/**
	 * Return true if the block cannot be broken, typically bedrock and lava
	 */
	public static boolean unbreakableBlock (int blockId) {
		return blockId == Block.bedrock.blockID
			|| blockId == Block.lavaStill.blockID
			|| blockId == Block.lavaMoving.blockID;
	}

	@Deprecated // To be removed
	public static void breakBlock(World world, int x, int y, int z) {
		int blockId = world.getBlockId(x, y, z);
		
		if (blockId != 0) {
			Block.blocksList[blockId].dropBlockAsItem(world, x, y, z,
					world.getBlockMetadata(x, y, z), 0);
		}				
		
		world.setBlockWithNotify(x, y, z, 0);
	}
	
	public static EntityPlayer getBuildCraftPlayer (World world) {
		if (buildCraftPlayer == null) {
			buildCraftPlayer = APIProxy.createNewPlayer (world);
		}
		
		return buildCraftPlayer;
	}
	
	public static void registerRefineryRecipe (RefineryRecipe recipe) {
		if (!refineryRecipe.contains(recipe)) {
			refineryRecipe.add(recipe);
		}
	}
	
	public static RefineryRecipe findRefineryRecipe(int liquid1, int qty1,
			int liquid2, int qty2) {
		int l1 = qty1 > 0 ? liquid1 : 0;
		int l2 = qty2 > 0 ? liquid2 : 0;

		for (RefineryRecipe r : refineryRecipe) {
			int src1 = 0;
			int src2 = 0;

			if (r.sourceId1 == l1) {
				src1 = l1;
				src2 = l2;
			} else if (r.sourceId1 == l2) {
				src1 = l2;
				src2 = l1;
			}

			if (src1 == 0) {
				continue;
			}

			if ((r.sourceQty2 == 0 && (src2 == 0 || src2 == src1))
					|| r.sourceId2 == src2) {
				return r;
			}
		}

		return null;
	}
	
	public static BlockSignature getBlockSignature (Block block) {	
		return blockBptProps [0].getSignature(block);
	}	
	
	public static ItemSignature getItemSignature(Item item) {
		ItemSignature sig = new ItemSignature();
		
		if (item.shiftedIndex >= Block.blocksList.length + BuildCraftAPI.LAST_ORIGINAL_ITEM) {
			sig.itemClassName = item.getClass().getSimpleName();	
		}
		
		sig.itemName = item.getItemNameIS(new ItemStack(item));
		
		return sig;
	}
	
	public static void registerTriggerProvider (ITriggerProvider provider) {
		if (provider != null && !triggerProviders.contains(provider)) {
			triggerProviders.add(provider);
		}
	}
	
	public static LinkedList <Trigger> getNeighborTriggers (Block block, TileEntity entity) {
		LinkedList <Trigger> triggers = new LinkedList <Trigger> ();
		
		for (ITriggerProvider provider : triggerProviders) {
			LinkedList <Trigger> toAdd = provider.getNeighborTriggers(block, entity);
		
			if (toAdd != null) {
				for (Trigger t : toAdd) {
					if (!triggers.contains(t)) {
						triggers.add(t);
					}
				}
			}
		}
		
		return triggers;
	}
	
	public static void registerActionProvider (IActionProvider provider) {
		if (provider != null && !actionProviders.contains(provider)) {
			actionProviders.add(provider);
		}
	}
	
	public static LinkedList <Action> getNeighborActions (Block block, TileEntity entity) {
		LinkedList <Action> actions = new LinkedList <Action> ();
		
		for (IActionProvider provider : actionProviders) {
			LinkedList <Action> toAdd = provider.getNeighborActions(block, entity);
		
			if (toAdd != null) {
				for (Action t : toAdd) {
					if (!actions.contains(t)) {
						actions.add(t);
					}
				}
			}
		}
		
		return actions;
	}
	
	public static LinkedList <Trigger> getPipeTriggers (IPipe pipe) {
		LinkedList <Trigger> triggers = new LinkedList <Trigger> ();
		
		for (ITriggerProvider provider : triggerProviders) {
			LinkedList <Trigger> toAdd = provider.getPipeTriggers(pipe);
		
			if (toAdd != null) {
				for (Trigger t : toAdd) {
					if (!triggers.contains(t)) {
						triggers.add(t);
					}
				}
			}
		}
		
		return triggers;
	}
	
	static {
		for (int i = 0; i < softBlocks.length; ++i) {
			softBlocks [i] = false;
		}
		
		// Initialize defaults for block properties.
		for (int i = 0; i < blockBptProps.length; ++i) {
			new BptBlock(i);
		}
	}
}
