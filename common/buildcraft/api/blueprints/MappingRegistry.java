/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import buildcraft.core.utils.Utils;

public class MappingRegistry {

	private HashMap <Block, Integer> blockToId = new HashMap<Block, Integer>();
	private HashMap <Integer, Block> idToBlock = new HashMap<Integer, Block>();

	private HashMap <Item, Integer> itemToId = new HashMap<Item, Integer>();
	private HashMap <Integer, Item> idToItem = new HashMap<Integer, Item>();

	private void setIdForItem (Item item, int id) {
		if (!itemToId.containsKey(item)) {
			itemToId.put(item, id);
			idToItem.put(id, item);
		}
	}

	private void setIdForBlock (Block block, int id) {
		if (!blockToId.containsKey(block)) {
			blockToId.put(block, id);
			idToBlock.put(id, block);
		}
	}

	public Item getItemForId(int id) {
		if (!idToItem.containsKey(id)) {
			return null;
		}

		return idToItem.get(id);
	}

	public int getIdForItem(Item item) {
		if (!itemToId.containsKey(item)) {
			setIdForItem(item, itemToId.size());
		}

		return itemToId.get(item);
	}

	public Block getBlockForId(int id) {
		if (!idToBlock.containsKey(id)) {
			return null;
		}

		return idToBlock.get(id);
	}

	public int getIdForBlock(Block block) {
		if (!blockToId.containsKey(block)) {
			setIdForBlock(block, blockToId.size());
		}

		return blockToId.get(block);
	}

	public void write (NBTTagCompound nbt) {
		NBTTagList blocksMapping = new NBTTagList();

		for (Entry<Block, Integer> e : blockToId.entrySet()) {
			NBTTagCompound sub = new NBTTagCompound();
			sub.setString("name",
					Block.blockRegistry.getNameForObject(e.getKey()));
			sub.setInteger("id", e.getValue());
			blocksMapping.appendTag(sub);
		}

		nbt.setTag("blocksMapping", blocksMapping);

		NBTTagList itemsMapping = new NBTTagList();

		for (Entry<Item, Integer> e : itemToId.entrySet()) {
			NBTTagCompound sub = new NBTTagCompound();
			sub.setString("name",
					Item.itemRegistry.getNameForObject(e.getKey()));
			sub.setInteger("id", e.getValue());
			itemsMapping.appendTag(sub);
		}

		nbt.setTag("itemsMapping", itemsMapping);
	}

	public void read (NBTTagCompound nbt) {
		NBTTagList blocksMapping = nbt.getTagList("blocksMapping",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		for (int i = 0; i < blocksMapping.tagCount(); ++i) {
			NBTTagCompound sub = blocksMapping.getCompoundTagAt(i);

			int id = sub.getInteger("id");
			String name = sub.getString("name");

			Block b = (Block) Block.blockRegistry.getObject(name);

			setIdForBlock(b, id);
		}

		NBTTagList itemsMapping = nbt.getTagList("itemsMapping",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		for (int i = 0; i < itemsMapping.tagCount(); ++i) {
			NBTTagCompound sub = itemsMapping.getCompoundTagAt(i);

			int id = sub.getInteger("id");
			String name = sub.getString("name");

			Item item = (Item) Item.itemRegistry.getObject(name);

			setIdForItem(item, id);
		}
	}

	@Override
	public final MappingRegistry clone() {
		MappingRegistry result = new MappingRegistry();
		result.blockToId = (HashMap<Block, Integer>) blockToId.clone();
		result.idToBlock = (HashMap<Integer, Block>) idToBlock.clone();
		result.itemToId = (HashMap<Item, Integer>) itemToId.clone();
		result.idToItem = (HashMap<Integer, Item>) idToItem.clone();

		return result;
	}

}
