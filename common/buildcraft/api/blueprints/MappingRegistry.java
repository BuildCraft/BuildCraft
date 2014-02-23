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

	public void setIdForItem (Item item, int id) {
		if (!itemToId.containsKey(item)) {
			itemToId.put(item, id);
			idToItem.put(id, item);
		}
	}

	public void setIdForBlock (Block block, int id) {
		if (!blockToId.containsKey(block)) {
			blockToId.put(block, id);
			idToBlock.put(id, block);
		}
	}

	public Item getItemForId(int id) {
		if (idToItem.containsKey(id)) {
			return idToItem.get(id);
		}

		return null;
	}

	public int getIdForItem(Item item) {
		if (itemToId.containsKey(item)) {
			return itemToId.get(item);
		}

		return 0;
	}

	public Block getBlockForId(int id) {
		if (idToBlock.containsKey(id)) {
			return idToBlock.get(id);
		}

		return null;
	}

	public int getIdForBlock(Block block) {
		if (blockToId.containsKey(block)) {
			return blockToId.get(block);
		}

		return 0;
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
