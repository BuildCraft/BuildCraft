/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import buildcraft.core.network.NetworkData;
import buildcraft.core.utils.Utils;

public class MappingRegistry {

	@NetworkData
	public HashMap <Block, Integer> blockToId = new HashMap<Block, Integer>();

	@NetworkData
	public ArrayList <Block> idToBlock = new ArrayList<Block>();

	@NetworkData
	public HashMap <Item, Integer> itemToId = new HashMap<Item, Integer>();

	@NetworkData
	public ArrayList <Item> idToItem = new ArrayList<Item>();

	private void registerItem (Item item) {
		if (!itemToId.containsKey(item)) {
			idToItem.add(item);
			itemToId.put(item, idToItem.size() - 1);
		}
	}

	private void registerBlock (Block block) {
		if (!blockToId.containsKey(block)) {
			idToBlock.add(block);
			blockToId.put(block, idToBlock.size() - 1);
		}
	}

	public Item getItemForId(int id) {
		if (id >= idToItem.size()) {
			return null;
		}

		return idToItem.get(id);
	}

	public int getIdForItem(Item item) {
		if (!itemToId.containsKey(item)) {
			registerItem(item);
		}

		return itemToId.get(item);
	}

	public Block getBlockForId(int id) {
		if (id >= idToBlock.size()) {
			return null;
		}

		return idToBlock.get(id);
	}

	public int getIdForBlock(Block block) {
		if (!blockToId.containsKey(block)) {
			registerBlock (block);
		}

		return blockToId.get(block);
	}

	public void write (NBTTagCompound nbt) {
		NBTTagList blocksMapping = new NBTTagList();

		for (Block b : idToBlock) {
			NBTTagCompound sub = new NBTTagCompound();
			sub.setString("name",
					Block.blockRegistry.getNameForObject(b));
			blocksMapping.appendTag(sub);
		}

		nbt.setTag("blocksMapping", blocksMapping);

		NBTTagList itemsMapping = new NBTTagList();

		for (Item i : idToItem) {
			NBTTagCompound sub = new NBTTagCompound();
			sub.setString("name",
					Item.itemRegistry.getNameForObject(i));
			itemsMapping.appendTag(sub);
		}

		nbt.setTag("itemsMapping", itemsMapping);
	}

	public void read (NBTTagCompound nbt) {
		NBTTagList blocksMapping = nbt.getTagList("blocksMapping",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		for (int i = 0; i < blocksMapping.tagCount(); ++i) {
			NBTTagCompound sub = blocksMapping.getCompoundTagAt(i);
			String name = sub.getString("name");
			Block b = (Block) Block.blockRegistry.getObject(name);
			registerBlock (b);
		}

		NBTTagList itemsMapping = nbt.getTagList("itemsMapping",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		for (int i = 0; i < itemsMapping.tagCount(); ++i) {
			NBTTagCompound sub = itemsMapping.getCompoundTagAt(i);
			String name = sub.getString("name");
			Item item = (Item) Item.itemRegistry.getObject(name);
			registerItem (item);
		}
	}

	@Override
	public final MappingRegistry clone() {
		MappingRegistry result = new MappingRegistry();
		result.blockToId = (HashMap<Block, Integer>) blockToId.clone();
		result.idToBlock = (ArrayList<Block>) idToBlock.clone();
		result.itemToId = (HashMap<Item, Integer>) itemToId.clone();
		result.idToItem = (ArrayList<Item>) idToItem.clone();

		return result;
	}

}
