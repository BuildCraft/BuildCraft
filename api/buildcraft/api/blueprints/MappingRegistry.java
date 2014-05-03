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
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

public class MappingRegistry {

	public HashMap<Block, Integer> blockToId = new HashMap<Block, Integer>();
	public ArrayList<Block> idToBlock = new ArrayList<Block>();

	public HashMap<Item, Integer> itemToId = new HashMap<Item, Integer>();
	public ArrayList<Item> idToItem = new ArrayList<Item>();

	public HashMap<Class<? extends Entity>, Integer> entityToId = new HashMap<Class<? extends Entity>, Integer>();
	public ArrayList<Class<? extends Entity>> idToEntity = new ArrayList<Class<? extends Entity>>();

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

	private void registerEntity(Class<? extends Entity> entityClass) {
		if (!entityToId.containsKey(entityClass)) {
			idToEntity.add(entityClass);
			entityToId.put(entityClass, idToEntity.size() - 1);
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

	public Class<? extends Entity> getEntityForId(int id) {
		if (id >= idToEntity.size()) {
			return null;
		}

		return idToEntity.get(id);
	}

	public int getIdForEntity(Class<? extends Entity> entity) {
		if (!entityToId.containsKey(entity)) {
			registerEntity (entity);
		}

		return entityToId.get(entity);
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

		NBTTagList entitiesMapping = new NBTTagList();

		for (Class<? extends Entity> e : idToEntity) {
			NBTTagCompound sub = new NBTTagCompound();
			sub.setString("name", e.getCanonicalName());
			entitiesMapping.appendTag(sub);
		}

		nbt.setTag("entitiesMapping", entitiesMapping);
	}

	public void read (NBTTagCompound nbt) {
		NBTTagList blocksMapping = nbt.getTagList("blocksMapping",
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < blocksMapping.tagCount(); ++i) {
			NBTTagCompound sub = blocksMapping.getCompoundTagAt(i);
			String name = sub.getString("name");
			Block b = (Block) Block.blockRegistry.getObject(name);
			registerBlock (b);
		}

		NBTTagList itemsMapping = nbt.getTagList("itemsMapping",
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < itemsMapping.tagCount(); ++i) {
			NBTTagCompound sub = itemsMapping.getCompoundTagAt(i);
			String name = sub.getString("name");
			Item item = (Item) Item.itemRegistry.getObject(name);
			registerItem (item);
		}

		NBTTagList entitiesMapping = nbt.getTagList("entitiesMapping",
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < entitiesMapping.tagCount(); ++i) {
			NBTTagCompound sub = entitiesMapping.getCompoundTagAt(i);
			String name = sub.getString("name");
			Class<? extends Entity> e = null;

			try {
				e = (Class<? extends Entity>) Class.forName(name);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			registerEntity (e);
		}
	}
}
