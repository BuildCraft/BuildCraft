/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.builders.blueprints.BlueprintBuilder.SchematicBuilder;
import buildcraft.builders.blueprints.IBlueprintBuilderAgent;

/**
 * BlockHandlers are used to serialize blocks for saving/loading from
 * Blueprints.
 *
 * To implement your own, you should extend this class and override the
 * functions as needed.
 */
public class BlockHandler {

	private static final Map<Object, BlockHandler> handlers = new HashMap<Object, BlockHandler>();
	private final int id;

	public static BlockHandler get(Item item) {
		if (item == null) {
			return null;
		} else {
			return get(item);
		}
	}

	public static BlockHandler get(Block block) {
		if (block == null) {
			return null;
		} else {
			return get(block);
		}
	}

	public static BlockHandler get(int id) {
		BlockHandler handler = handlers.get(id);

		if (handler == null) {
			handler = new BlockHandler(id);
			registerHandler(id, handler);
		}

		return handler;
	}

	public static void registerHandler(Block block, BlockHandler handler) {
		handlers.put(block, handler);
	}

	public static void registerHandler(Item item, BlockHandler handler) {
		handlers.put(item, handler);
	}

	public static void registerHandler(int id, BlockHandler handler) {
		handlers.put(id, handler);
	}

	// FIXME: This way of creating handlers not related to a particular block
	// is a bit off - to be improved.
	public BlockHandler() {
		this.id = 0;
	}

	public BlockHandler(int id) {
		this.id = id;
	}

	/**
	 * By default we will ignore all blocks with Tile Entities.
	 *
	 * We will also skip any blocks that drop actual items like Ore blocks.
	 */
	public boolean canSaveToSchematic(World world, int x, int y, int z) {
		Block block = Block.getBlockById(id);

		if (block == null) {
			return false;
		}

		int meta = world.getBlockMetadata(x, y, z);

		try {
			if (block.getItemDropped(meta, null, 0) != Item.getItemFromBlock(block)) {
				return false;
			}
		} catch (NullPointerException ex) {
			return false;
		}

		return !block.hasTileEntity(meta);
	}

	/**
	 * By default we will ignore all blocks with Tile Entities.
	 *
	 * We will also ignore anything that's not a ItemBlock.
	 *
	 * We will also skip any blocks that drop actual items like Ore blocks.
	 */
	public boolean canSaveToSchematic(ItemStack stack) {
		if (stack == null) {
			return false;
		}

		if (!(stack.getItem() instanceof ItemBlock)) {
			return false;
		}

		Block block = Block.getBlockById(id);

		if (block == null) {
			return false;
		}

		try {
			if (block.getItemDropped(stack.getItemDamage(), null, 0) != Item.getItemFromBlock(block)) {
				return false;
			}
		} catch (NullPointerException ex) {
			return false;
		}
		return !block.hasTileEntity(stack.getItemDamage());
	}

	/**
	 * It is assumed that Blueprints always face North on save.
	 *
	 * Store any info you need to reproduce the block in the data tag.
	 */
	public void saveToSchematic(World world, int x, int y, int z, NBTTagCompound data) {
		data.setByte("blockMeta", (byte) world.getBlockMetadata(x, y, z));
	}

	/**
	 * It is assumed that Blueprints always face North on save.
	 *
	 * Store any info you need to reproduce the block from this ItemStack in the
	 * data tag.
	 */
	public void saveToSchematic(ItemStack stack, NBTTagCompound data) {
		if (stack.getHasSubtypes())
			data.setByte("blockMeta", (byte) stack.getItemDamage());
	}

	/**
	 * Provide a list of all the items that must be present to build this
	 * schematic.
	 *
	 * If you need axillary items like a painter or gate, list them as well.
	 * Items will be consumed in the readBlockFromSchematic() function below.
	 *
	 * This default implementation will only work for simple blocks without tile
	 * entities and will in fact break on Ore blocks as well. Which is why those
	 * blocks can't be saved by default.
	 */
	public List<ItemStack> getCostForSchematic(NBTTagCompound data) {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		Block block = Block.getBlockById(id);
		cost.add(new ItemStack(block.getItemDropped(data.getByte("blockMeta"), BlueprintHelpers.RANDOM, 0), 1, block.damageDropped(data.getByte("blockMeta"))));

		return cost;
	}

	protected final boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null)
			return false;
		if (!stack1.isItemEqual(stack2))
			return false;
		if (!ItemStack.areItemStackTagsEqual(stack1, stack2))
			return false;
		return true;

	}

	/**
	 * Can the block be placed currently or is it waiting on some other block to
	 * be placed first?
	 */
	public boolean canPlaceNow(World world, int x, int y, int z, ForgeDirection blueprintOrientation, NBTTagCompound data) {
		return true;
	}

	/**
	 * This function handles the placement of the block in the world.
	 *
	 * The ForgeDirection parameter can be use to determine the orientation of
	 * the blueprint. Blueprints are always saved facing North. This function
	 * will have to rotate the block accordingly.
	 *
	 * The builder's inventory is passed in so you can consume the items you
	 * need. Use them as you see fit.
	 *
	 * If the function returns false, the block was not placed. You should not
	 * modify any ItemStack in the inventory until you have determined that
	 * everything you require is present.
	 */
	public boolean buildBlockFromSchematic(World world, SchematicBuilder builder, IBlueprintBuilderAgent builderAgent) {
		IInventory builderInventory = builderAgent.getInventory();

		if (builderInventory != null) {
			List<ItemStack> requiredItems = getCostForSchematic(builder.schematic.data);
			List<Integer> slotsToConsume = new ArrayList<Integer>();

			for (ItemStack cost : requiredItems) {
				boolean found = false;

				for (int slot = 0; slot < builderInventory.getSizeInventory(); slot++) {
					if (areItemsEqual(builderInventory.getStackInSlot(slot), cost)) {
						slotsToConsume.add(slot);
						found = true;
						break;
					}
				}

				if (!found) {
					return false;
				}
			}
			for (Integer slot : slotsToConsume) {
				builderInventory.setInventorySlotContents(slot, BlueprintHelpers.consumeItem(builderInventory.getStackInSlot(slot)));
			}
		}

		return world.setBlock(builder.getX(), builder.getY(), builder.getZ(), Block.getBlockById(id), 0, 3);
	}

	/**
	 * Checks if the block matches the schematic.
	 */
	public boolean doesBlockMatchSchematic(World world, int x, int y, int z, ForgeDirection blueprintOrientation, NBTTagCompound data) {
		if (Block.getBlockById(id) != world.getBlock(x, y, z)) {
			return false;
		}

		return !data.hasKey("blockMeta") || data.getByte("blockMeta") == world.getBlockMetadata(x, y, z);
	}

	public boolean isComplete(World worldObj, SchematicBuilder schematicBuilder) {
		return false;
	}
}
