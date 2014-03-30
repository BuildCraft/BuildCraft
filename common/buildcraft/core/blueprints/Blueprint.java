/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.CoordTransformation;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.NBTUtils;
import buildcraft.core.utils.Utils;

public class Blueprint extends BlueprintBase {
	public LinkedList <SchematicEntity> entities = new LinkedList <SchematicEntity> ();

	public Blueprint() {
		super ();
	}

	public Blueprint(int sizeX, int sizeY, int sizeZ) {
		super(sizeX, sizeY, sizeZ);
	}

	@Override
	public void rotateLeft(BptContext context) {
		for (SchematicEntity e : entities) {
			e.rotateLeft(context);
		}

		super.rotateLeft(context);
	}

	@Override
	public void readFromWorld(IBuilderContext context, TileEntity anchorTile, int x, int y, int z) {
		BptContext bptContext = (BptContext) context;
		Block block = anchorTile.getWorldObj().getBlock(x, y, z);

		SchematicBlock slot = SchematicRegistry.newSchematicBlock(block);

		if (slot == null) {
			return;
		}

		int posX = (int) (x - context.surroundingBox().pMin().x);
		int posY = (int) (y - context.surroundingBox().pMin().y);
		int posZ = (int) (z - context.surroundingBox().pMin().z);

		slot.block = block;
		slot.meta = anchorTile.getWorldObj().getBlockMetadata(x, y, z);

		if (!bptContext.readConfiguration.readTiles && anchorTile.getWorldObj().getTileEntity(x, y, z) != null) {
			return;
		}

		try {
			slot.readFromWorld(context, x, y, z);
			contents[posX][posY][posZ] = slot;
		} catch (Throwable t) {
			// Defensive code against errors in implementers
			t.printStackTrace();
			BCLog.logger.throwing("BptBlueprint", "readFromWorld", t);
		}
	}

	@Override
	public void readEntitiesFromWorld(IBuilderContext context, TileEntity anchorTile) {
		CoordTransformation transform = new CoordTransformation();

		transform.x = -context.surroundingBox().pMin().x;
		transform.y = -context.surroundingBox().pMin().y;
		transform.z = -context.surroundingBox().pMin().z;

		for (Object o : context.world().loadedEntityList) {
			Entity e = (Entity) o;

			if (context.surroundingBox().contains(e.posX, e.posY, e.posZ)) {
				SchematicEntity s = SchematicRegistry.newSchematicEntity(e.getClass());

				if (s != null) {
					s.readFromWorld(context, e, transform);
					entities.add(s);
				}
			}
		}
	}


	@Override
	public void saveContents(NBTTagCompound nbt) {
		NBTTagList nbtContents = new NBTTagList();

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					NBTTagCompound cpt = new NBTTagCompound();

					if (contents [x][y][z] != null) {
						contents[x][y][z].writeToNBT(cpt, mapping);
					}

					nbtContents.appendTag(cpt);
				}
			}
		}

		nbt.setTag("contents", nbtContents);

		NBTTagList entitiesNBT = new NBTTagList();

		for (SchematicEntity s : entities) {
			NBTTagCompound subNBT = new NBTTagCompound();
			s.writeToNBT(subNBT, mapping);
			entitiesNBT.appendTag(subNBT);
		}

		nbt.setTag("entities", entitiesNBT);

		NBTTagCompound contextNBT = new NBTTagCompound();
		mapping.write (contextNBT);
		nbt.setTag("idMapping", contextNBT);
	}

	@Override
	public void loadContents(NBTTagCompound nbt) throws BptError {
		mapping.read (nbt.getCompoundTag("idMapping"));

		NBTTagList nbtContents = nbt.getTagList("contents",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		int index = 0;

		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					NBTTagCompound cpt = nbtContents.getCompoundTagAt(index);
					index++;

					if (cpt.hasKey("blockId")) {
						int blockId = cpt.getInteger("blockId");

						contents[x][y][z] = SchematicRegistry.newSchematicBlock(mapping.getBlockForId(blockId));
						contents[x][y][z].readFromNBT(cpt, mapping);
					} else {
						contents[x][y][z] = null;
					}
				}
			}
		}

		NBTTagList entitiesNBT = nbt.getTagList("entities",
				Utils.NBTTag_Types.NBTTagCompound.ordinal());

		for (int i = 0; i < entitiesNBT.tagCount(); ++i) {
			NBTTagCompound cpt = entitiesNBT.getCompoundTagAt(i);

			if (cpt.hasKey("entityId")) {
				int entityId = cpt.getInteger("entityId");
				SchematicEntity s = SchematicRegistry.newSchematicEntity(mapping.getEntityForId(entityId));
				s.readFromNBT(cpt, mapping);
				entities.add(s);
			}
		}
	}

	@Override
	public ItemStack getStack () {
		ItemStack stack = new ItemStack(BuildCraftBuilders.blueprintItem, 1);
		NBTTagCompound nbt = NBTUtils.getItemData(stack);
		id.write (nbt);
		nbt.setString("author", author);

		return stack;
	}
}
