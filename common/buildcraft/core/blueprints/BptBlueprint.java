/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.blueprints;

import buildcraft.api.blueprints.BlockSignature;
import buildcraft.api.blueprints.BlueprintManager;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.api.blueprints.ItemSignature;
import buildcraft.core.IBptContributor;
import buildcraft.core.utils.BCLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.TreeSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class BptBlueprint extends BptBase {

	private int[] idMapping = new int[Item.itemsList.length];

	TreeSet<Integer> idsToMap = new TreeSet<Integer>();

	public BptBlueprint() {
		for (int i = 0; i < idMapping.length; ++i) {
			idMapping[i] = i;
		}
	}

	public BptBlueprint(int sizeX, int sizeY, int sizeZ) {
		super(sizeX, sizeY, sizeZ);

		for (int i = 0; i < idMapping.length; ++i) {
			idMapping[i] = i;
		}
	}

	public void readFromWorld(IBptContext context, TileEntity anchorTile, int x, int y, int z) {
		BptSlot slot = new BptSlot();

		slot.x = (int) (x - context.surroundingBox().pMin().x);
		slot.y = (int) (y - context.surroundingBox().pMin().y);
		slot.z = (int) (z - context.surroundingBox().pMin().z);
		slot.blockId = anchorTile.worldObj.getBlockId(x, y, z);
		slot.meta = anchorTile.worldObj.getBlockMetadata(x, y, z);

		if (Block.blocksList[slot.blockId] instanceof BlockContainer) {
			TileEntity tile = anchorTile.worldObj.getBlockTileEntity(x, y, z);

			if (tile != null && tile instanceof IBptContributor) {
				IBptContributor contributor = (IBptContributor) tile;

				contributor.saveToBluePrint(anchorTile, this, slot);
			}
		}

		try {
			slot.initializeFromWorld(context, x, y, z);
			contents[slot.x][slot.y][slot.z] = slot;
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBlueprint", "readFromWorld", t);

		}
	}

	@Override
	public void saveAttributes(BufferedWriter writer) throws IOException {
		writer.write("sizeX:" + sizeX);
		writer.newLine();
		writer.write("sizeY:" + sizeY);
		writer.newLine();
		writer.write("sizeZ:" + sizeZ);
		writer.newLine();
		writer.write("anchorX:" + anchorX);
		writer.newLine();
		writer.write("anchorY:" + anchorY);
		writer.newLine();
		writer.write("anchorZ:" + anchorZ);
		writer.newLine();

		boolean[] idsUsed = new boolean[Item.itemsList.length];

		for (int i = 1; i < idsUsed.length; ++i) {
			idsUsed[i] = false;
		}

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					BptSlotInfo slot = contents[x][y][z];

					storeId(slot.blockId);

					for (ItemStack stack : slot.storedRequirements) {
						storeId(stack.itemID);
					}
				}
			}
		}

		writer.write("idMap:");
		writer.newLine();

		for (Integer id : idsToMap) {
			if (id < Block.blocksList.length && Block.blocksList[id] != null) {
				writer.write(BlueprintManager.getBlockSignature(Block.blocksList[id]) + "=" + id);
			} else {
				writer.write(BlueprintManager.getItemSignature(Item.itemsList[id]) + "=" + id);
			}

			writer.newLine();
		}

		writer.write(":idMap");
		writer.newLine();

		writer.write("contents:");
		writer.newLine();

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					BptSlotInfo slot = contents[x][y][z];

					if (slot != null && slot.blockId != 0) {
						slot.cpt.setInteger("bId", slot.blockId);

						if (slot.meta != 0) {
							slot.cpt.setInteger("meta", slot.meta);
						}

						NBTBase.writeNamedTag(slot.cpt, new BptDataStream(writer));

						writer.newLine();
					} else {
						writer.newLine();
					}
				}
			}
		}

		writer.write(":contents");
		writer.newLine();

		writer.write("requirements:");
		writer.newLine();

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					BptSlotInfo slot = contents[x][y][z];

					if (slot != null && slot.blockId != 0 && slot.storedRequirements.size() > 0) {
						NBTTagList list = new NBTTagList();

						for (ItemStack stack : slot.storedRequirements) {
							NBTTagCompound sub = new NBTTagCompound();
							stack.writeToNBT(sub);
							list.appendTag(sub);
						}

						NBTBase.writeNamedTag(list, new BptDataStream(writer));

						writer.newLine();
					} else {
						writer.newLine();
					}
				}
			}
		}

		writer.write(":requirements");
		writer.newLine();
	}

	@Override
	public void loadAttribute(BufferedReader reader, String attr, String val) throws IOException, BptError {
		if ("3.1.0".equals(version))
			throw new BptError("Blueprint format 3.1.0 is not supported anymore, can't load " + file);

		// blockMap is still tested for being able to load pre 3.1.2 bpts
		if (attr.equals("blockMap") || attr.equals("idMap")) {
			while (true) {
				String mapStr = reader.readLine();

				if (mapStr == null) {
					break;
				}

				mapStr = mapStr.replaceAll("\n", "");

				if (mapStr.equals(":blockMap") || mapStr.equals(":idMap"))
					return;

				String[] parts = mapStr.split("=");
				int blockId = Integer.parseInt(parts[1]);

				if (parts[0].startsWith("#I")) {
					ItemSignature sig = new ItemSignature(parts[0]);
					int itemId = Integer.parseInt(parts[1]);

					if (!itemMatch(sig, Item.itemsList[itemId])) {
						boolean found = false;
						for (int i = 256; i < Item.itemsList.length; ++i) {

							// Items between 256 and Block.blocksList.length may
							// be item or block
							if (i < Block.blocksList.length && Block.blocksList[i] != null) {
								continue;
							}

							if (itemMatch(sig, Item.itemsList[i])) {
								found = true;
								idMapping[itemId] = i;
								break;
							}
						}

						if (!found)
							throw new BptError("BLUEPRINT ERROR: can't find item of signature " + sig + " for " + name);
					}

				} else {
					BlockSignature bptSignature = new BlockSignature(parts[0]);
					BptBlock defaultBlock = BlueprintManager.blockBptProps[0];

					BptBlock handlingBlock = BlueprintManager.blockBptProps[blockId];

					if (handlingBlock == null) {
						handlingBlock = defaultBlock;
					}

					if (!handlingBlock.match(Block.blocksList[blockId], bptSignature)) {
						boolean found = false;

						for (int i = 0; i < Block.blocksList.length; ++i)
							if (Block.blocksList[i] != null) {
								handlingBlock = BlueprintManager.blockBptProps[i];

								if (handlingBlock == null) {
									handlingBlock = defaultBlock;
								}

								if (handlingBlock.match(Block.blocksList[i], bptSignature)) {
									idMapping[blockId] = i;
									found = true;
								}
							}

						if (!found)
							throw new BptError("BLUEPRINT ERROR: can't find block of signature " + bptSignature + " for " + name);
					}
				}
			}
		} else if (attr.equals("contents")) {
			contents = new BptSlot[sizeX][sizeY][sizeZ];

			for (int x = 0; x < sizeX; ++x) {
				for (int y = 0; y < sizeY; ++y) {
					for (int z = 0; z < sizeZ; ++z) {
						String slotStr = reader.readLine().replaceAll("\n", "");

						if (slotStr.equals(":contents"))
							return;

						if (!slotStr.equals("")) {
							BptSlot slot = new BptSlot();
							slot.x = x;
							slot.y = y;
							slot.z = z;

							slot.cpt = (NBTTagCompound) NBTBase.readNamedTag(new BptDataStream(new StringReader(slotStr)));

							slot.blockId = mapWorldId(slot.cpt.getInteger("bId"));

							if (slot.cpt.hasKey("meta")) {
								slot.meta = slot.cpt.getInteger("meta");
							}

							contents[x][y][z] = slot;
						}
					}
				}
			}
		} else if (attr.equals("requirements")) {
			for (int x = 0; x < sizeX; ++x) {
				for (int y = 0; y < sizeY; ++y) {
					for (int z = 0; z < sizeZ; ++z) {
						String reqStr = reader.readLine().replaceAll("\n", "");

						if (reqStr.equals(":requirements"))
							return;

						if (!reqStr.equals("")) {
							NBTTagList list = (NBTTagList) NBTBase.readNamedTag(new BptDataStream(new StringReader(reqStr)));

							for (int i = 0; i < list.tagCount(); ++i) {
								ItemStack stk = mapItemStack(ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i)));

								contents[x][y][z].storedRequirements.add(stk);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		if (name == null)
			return "Blueprint #" + position;
		else
			return name;
	}

	public ItemStack mapItemStack(ItemStack bptItemStack) {
		ItemStack newStack = bptItemStack.copy();

		newStack.itemID = idMapping[newStack.itemID];

		return newStack;
	}

	public int mapWorldId(int bptWorldId) {
		return idMapping[bptWorldId];
	}

	public void storeId(int worldId) {
		if (worldId != 0) {
			idsToMap.add(worldId);
		}
	}

	private boolean itemMatch(ItemSignature sig, Item item) {
		if (item == null)
			return false;

		if (!"*".equals(sig.itemClassName) && !item.getClass().getSimpleName().equals(sig.itemClassName))
			return false;

		if (!"*".equals(sig.itemName) && !item.getUnlocalizedName(new ItemStack(item)).equals(sig.itemName))
			return false;

		return true;
	}

	@Override
	public void setBlockId(int x, int y, int z, int blockId) {
		super.setBlockId(x, y, z, blockId);
	}

	@Override
	protected void copyTo(BptBase bpt) {
		((BptBlueprint) bpt).idMapping = idMapping.clone();
	}
}
