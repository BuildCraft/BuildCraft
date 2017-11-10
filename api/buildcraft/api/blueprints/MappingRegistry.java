/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.BCLog;

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

	public Item getItemForId(int id) throws MappingNotFoundException {
		if (id >= idToItem.size()) {
			throw new MappingNotFoundException("no item mapping at position " + id);
		}

		Item result = idToItem.get(id);

		if (result == null) {
			throw new MappingNotFoundException("no item mapping at position " + id);
		} else {
			return result;
		}
	}

	public int getIdForItem(Item item) {
		if (!itemToId.containsKey(item)) {
			registerItem(item);
		}

		return itemToId.get(item);
	}

	public int itemIdToRegistry(int id) {
		Item item = Item.getItemById(id);

		return getIdForItem(item);
	}

	public int itemIdToWorld(int id) throws MappingNotFoundException {
		Item item = getItemForId(id);

		return Item.getIdFromItem(item);
	}

	public Block getBlockForId(int id) throws MappingNotFoundException {
		if (id >= idToBlock.size()) {
			throw new MappingNotFoundException("no block mapping at position " + id);
		}

		Block result = idToBlock.get(id);

		if (result == null) {
			throw new MappingNotFoundException("no block mapping at position " + id);
		} else {
			return result;
		}
	}

	public int getIdForBlock(Block block) {
		if (!blockToId.containsKey(block)) {
			registerBlock (block);
		}

		return blockToId.get(block);
	}

	public int blockIdToRegistry(int id) {
		Block block = Block.getBlockById(id);

		return getIdForBlock(block);
	}

	public int blockIdToWorld(int id) throws MappingNotFoundException {
		Block block = getBlockForId(id);

		return Block.getIdFromBlock(block);
	}

	public Class<? extends Entity> getEntityForId(int id) throws MappingNotFoundException {
		if (id >= idToEntity.size()) {
			throw new MappingNotFoundException("no entity mapping at position " + id);
		}

		Class<? extends Entity> result = idToEntity.get(id);

		if (result == null) {
			throw new MappingNotFoundException("no entity mapping at position " + id);
		} else {
			return result;
		}
	}

	public int getIdForEntity(Class<? extends Entity> entity) {
		if (!entityToId.containsKey(entity)) {
			registerEntity (entity);
		}

		return entityToId.get(entity);
	}

	/**
	 * Relocates a stack nbt from the world referential to the registry
	 * referential.
	 */
	public void stackToRegistry(NBTTagCompound nbt) {
		Item item = Item.getItemById(nbt.getShort("id"));
		nbt.setShort("id", (short) getIdForItem(item));
	}

	/**
	 * Relocates a stack nbt from the registry referential to the world
	 * referential.
	 */
	public void stackToWorld(NBTTagCompound nbt) throws MappingNotFoundException {
		Item item = getItemForId(nbt.getShort("id"));
		nbt.setShort("id", (short) Item.getIdFromItem(item));
	}

	private boolean isStackLayout(NBTTagCompound nbt) {
		return nbt.hasKey("id") &&
				nbt.hasKey("Count") &&
				nbt.hasKey("Damage") &&
				nbt.getTag("id") instanceof NBTTagShort &&
				nbt.getTag("Count") instanceof NBTTagByte &&
				nbt.getTag("Damage") instanceof NBTTagShort;
	}

	public void scanAndTranslateStacksToRegistry(NBTTagCompound nbt) {
		// First, check if this nbt is itself a stack

		if (isStackLayout(nbt)) {
			stackToRegistry(nbt);
		}

		// Then, look at the nbt compound contained in this nbt (even if it's a
		// stack) and checks for stacks in it.
		for (Object keyO : nbt.func_150296_c()) {
			String key = (String) keyO;

			if (nbt.getTag(key) instanceof NBTTagCompound) {
				scanAndTranslateStacksToRegistry(nbt.getCompoundTag(key));
			}

			if (nbt.getTag(key) instanceof NBTTagList) {
				NBTTagList list = (NBTTagList) nbt.getTag(key);

				if (list.func_150303_d() == Constants.NBT.TAG_COMPOUND) {
					for (int i = 0; i < list.tagCount(); ++i) {
						scanAndTranslateStacksToRegistry(list.getCompoundTagAt(i));
					}
				}
			}
		}
	}

	public void scanAndTranslateStacksToWorld(NBTTagCompound nbt) throws MappingNotFoundException {
		// First, check if this nbt is itself a stack

		if (isStackLayout(nbt)) {
			stackToWorld(nbt);
		}

		// Then, look at the nbt compound contained in this nbt (even if it's a
		// stack) and checks for stacks in it.
		for (Object keyO : new HashSet(nbt.func_150296_c())) {
			String key = (String) keyO;

			if (nbt.getTag(key) instanceof NBTTagCompound) {
				try {
					scanAndTranslateStacksToWorld(nbt.getCompoundTag(key));
				} catch (MappingNotFoundException e) {
					nbt.removeTag(key);
				}
			}

			if (nbt.getTag(key) instanceof NBTTagList) {
				NBTTagList list = (NBTTagList) nbt.getTag(key);

				if (list.func_150303_d() == Constants.NBT.TAG_COMPOUND) {
					for (int i = list.tagCount() - 1; i >= 0; --i) {
						try {
							scanAndTranslateStacksToWorld(list.getCompoundTagAt(i));
						} catch (MappingNotFoundException e) {
							list.removeTag(i);
						}
					}
				}
			}
		}
	}

	public void write (NBTTagCompound nbt) {
		NBTTagList blocksMapping = new NBTTagList();

		for (Block b : idToBlock) {
			NBTTagCompound sub = new NBTTagCompound();
			if (b != null) {
				String name = Block.blockRegistry.getNameForObject(b);
				if (name == null || name.length() == 0) {
					BCLog.logger.error("Block " + b.getUnlocalizedName() + " (" + b.getClass().getName() + ") has an empty registry name! This is a bug!");
				} else {
					sub.setString("name", name);
				}
			}
			blocksMapping.appendTag(sub);
		}

		nbt.setTag("blocksMapping", blocksMapping);

		NBTTagList itemsMapping = new NBTTagList();

		for (Item i : idToItem) {
			NBTTagCompound sub = new NBTTagCompound();
			if (i != null) {
				String name = Item.itemRegistry.getNameForObject(i);
				if (name == null || name.length() == 0) {
					BCLog.logger.error("Item " + i.getUnlocalizedName() + " (" + i.getClass().getName() + ") has an empty registry name! This is a bug!");
				} else {
					sub.setString("name", name);
				}
			}
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

//		System.out.println("[W] idToItem size : " + idToItem.size());
//		for (Item i : idToItem) {
//			System.out.println("- " + (i != null ? i.toString() : "null"));
//		}
	}

	private Object getMissingMappingFromFML(boolean isBlock, String name, int i) {
		String modName = name.split(":")[0];
		if (Loader.isModLoaded(modName)) {
			try {
				FMLMissingMappingsEvent.MissingMapping mapping = new FMLMissingMappingsEvent.MissingMapping(
						(isBlock ? '\u0001' : '\u0020') + name, i
				);
				ListMultimap<String, FMLMissingMappingsEvent.MissingMapping> missingMapping
						= ArrayListMultimap.create();
				missingMapping.put(modName, mapping);
				FMLMissingMappingsEvent event = new FMLMissingMappingsEvent(missingMapping);
				for (ModContainer container : Loader.instance().getModList()) {
					if (container instanceof FMLModContainer) {
						event.applyModContainer(container);
						((FMLModContainer) container).handleModStateEvent(event);
						if (mapping.getAction() != FMLMissingMappingsEvent.Action.DEFAULT) {
							break;
						}
					}
				}
				if (mapping.getAction() == FMLMissingMappingsEvent.Action.REMAP) {
					return mapping.getTarget();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void read (NBTTagCompound nbt) {
		NBTTagList blocksMapping = nbt.getTagList("blocksMapping",
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < blocksMapping.tagCount(); ++i) {
			NBTTagCompound sub = blocksMapping.getCompoundTagAt(i);
			if (!sub.hasKey("name")) {
				// Keeping the order correct
				idToBlock.add(null);
				BCLog.logger.log(Level.WARN, "Can't load a block - corrupt blueprint!");
				continue;
			}
			String name = sub.getString("name");
			Block b = null;

			if (!Block.blockRegistry.containsKey(name) && name.contains(":")) {
				b = (Block) getMissingMappingFromFML(true, name, i);
				if (b != null) {
					BCLog.logger.info("Remapped " + name + " to " + Block.blockRegistry.getNameForObject(b));
				}
			}
			
			if (b == null && Block.blockRegistry.containsKey(name)) {
				b = (Block) Block.blockRegistry.getObject(name);
			}
			
			if (b != null) {
				registerBlock(b);
			} else {
				// Keeping the order correct
				idToBlock.add(null);
				BCLog.logger.log(Level.WARN, "Can't load block " + name);
			}
		}

		NBTTagList itemsMapping = nbt.getTagList("itemsMapping",
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < itemsMapping.tagCount(); ++i) {
			NBTTagCompound sub = itemsMapping.getCompoundTagAt(i);
			if (!sub.hasKey("name")) {
				// Keeping the order correct
				idToItem.add(null);
				BCLog.logger.log(Level.WARN, "Can't load an item - corrupt blueprint!");
				continue;
			}

			String name = sub.getString("name");
			Item item = null;

			if (!Item.itemRegistry.containsKey(name) && name.contains(":")) {
				item = (Item) getMissingMappingFromFML(false, name, i);
				if (item != null) {
					BCLog.logger.info("Remapped " + name + " to " + Item.itemRegistry.getNameForObject(item));
				}
			}

			if (item == null && Item.itemRegistry.containsKey(name)) {
				item = (Item) Item.itemRegistry.getObject(name);
			}
			
			if (item != null) {
				registerItem(item);
			} else {
				// Keeping the order correct
				idToItem.add(null);
				BCLog.logger.log(Level.WARN, "Can't load item " + name);
			}
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

			if (e != null) {
				registerEntity(e);
			} else {
				// Keeping the order correct
				idToEntity.add(null);
				BCLog.logger.log(Level.WARN, "Can't load entity " + name);
			}
		}

//		System.out.println("[R] idToItem size : " + idToItem.size());
//		for (Item i : idToItem) {
//			System.out.println("- " + (i != null ? i.toString() : "null"));
//		}
	}
}
