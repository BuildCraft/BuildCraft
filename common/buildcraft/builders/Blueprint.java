/**
 * Copyright (c) SpaceToad, 2011-2012 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.BuildCraftCore;
import buildcraft.api.builder.BlockHandler;
import buildcraft.core.inventory.StackHelper;
import buildcraft.factory.TileQuarry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * This class is used to represent the data of the blueprint as it exists in the
 * world.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class Blueprint {

	private final String version = "Blueprint-2.0";
	private final UUID uuid;
	private String name;
	private String creator;
	private final BlockSchematic[][][] blocks;
	public final int sizeX, sizeY, sizeZ;
	public int anchorX, anchorY, anchorZ;
	public ForgeDirection anchorOrientation = ForgeDirection.NORTH;
	private List<ItemStack> costs;

	public Blueprint(int sizeX, int sizeY, int sizeZ) {
		this(sizeX, sizeY, sizeZ, UUID.randomUUID());
	}

	private Blueprint(int sizeX, int sizeY, int sizeZ, UUID uuid) {
		this.uuid = uuid;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		blocks = new BlockSchematic[sizeX][sizeY][sizeZ];
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private void setBlock(World world, int x, int y, int z, BlockSchematic schematic) {
		if (schematic == null)
			return;
		schematic.x = x;
		schematic.y = y;
		schematic.z = z;
		blocks[x][y][z] = schematic;
	}

	public void setBlock(World world, int x, int y, int z, Block block) {
		BlockHandler handler = BlockHandler.get(block);
		try {
			if (handler.canSaveBlockToSchematic(world, x, y, z)) {
				BlockSchematic schematic = new BlockSchematic(block);
				handler.saveToSchematic(world, x, y, z, schematic.blockData);
				setBlock(world, x, y, z, schematic);
			}
		} catch (Throwable error) {
			BuildCraftCore.bcLog.severe(String.format("Error while trying to save block [%s:%d] to blueprint, skipping.", block.getUnlocalizedName(), block.blockID));
			BuildCraftCore.bcLog.throwing(getClass().getCanonicalName(), "setBlock", error);
		}
	}

	/**
	 * Helper function for creating Blueprints in code.
	 *
	 * Not recommended for use with complex blocks because it doesn't go through
	 * a hander to get a BlockSchematic.
	 *
	 * @see TileQuarry
	 */
	public void setBlock(World world, int x, int y, int z, int id, int meta) {
		Block block = Block.blocksList[id];
		if (block == null) {
			return;
		}
		BlockSchematic schematic = new BlockSchematic(block);
		schematic.blockData.setByte("blockMeta", (byte) meta);
		setBlock(world, x, y, z, schematic);
	}

	public BlockSchematic getBlock(int x, int y, int z) {
		return blocks[x][y][z];
	}

	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Returns a list of all blocks in the Blueprint in the order they should be
	 * built.
	 *
	 * Be aware that changes to the Blueprint will not propagate to the list nor
	 * will changes to the list propagate to the Blueprint.
	 *
	 * @return List<BlockScematic>
	 */
	public LinkedList<BlockSchematic> getBuildList() {
		LinkedList<BlockSchematic> list = new LinkedList<BlockSchematic>();
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				for (int z = 0; z < sizeZ; z++) {
					if (blocks[x][y][z] != null)
						list.add(blocks[x][y][z]);
				}
			}
		}
		return list;
	}

	public List<ItemStack> getCost() {
		if (costs != null)
			return costs;
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		for (BlockSchematic schematic : getBuildList()) {
			BlockHandler handler = BlockHandler.get(schematic.block);
			List<ItemStack> requirements = handler.getCostForSchematic(schematic.blockData);
			for (ItemStack newStack : requirements) {
				if (newStack.stackSize <= 0)
					continue;
				for (ItemStack oldStack : stacks) {
					if (StackHelper.instance().canStacksMerge(oldStack, newStack)) {
						newStack.stackSize -= StackHelper.instance().mergeStacks(oldStack, newStack, true);
					}
				}
				if (newStack.stackSize > 0)
					stacks.add(newStack);
			}
		}
		costs = Collections.unmodifiableList(stacks);
		return costs;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList blockList = new NBTTagList();
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				for (int z = 0; z < sizeZ; z++) {
					if (blocks[x][y][z] == null)
						continue;
					NBTTagCompound blockNBT = new NBTTagCompound();
					blocks[x][y][z].writeToNBT(nbt);
					blockList.appendTag(blockNBT);
				}
			}
		}
		nbt.setTag("blocks", blockList);
		nbt.setLong("uuidMost", uuid.getMostSignificantBits());
		nbt.setLong("uuidLeast", uuid.getLeastSignificantBits());
		nbt.setString("name", name);
		nbt.setString("version", version);
		nbt.setString("creator", creator);
		nbt.setInteger("sizeX", sizeX);
		nbt.setInteger("sizeY", sizeY);
		nbt.setInteger("sizeZ", sizeZ);
		nbt.setInteger("anchorX", sizeX);
		nbt.setInteger("anchorY", sizeY);
		nbt.setInteger("anchorZ", sizeZ);
		nbt.setByte("anchorOrientation", (byte) anchorOrientation.ordinal());
	}

	public static Blueprint readFromNBT(NBTTagCompound nbt) {
		long most = nbt.getLong("uuidMost");
		long least = nbt.getLong("uuidLeast");
		int sizeX = nbt.getInteger("sizeX");
		int sizeY = nbt.getInteger("sizeY");
		int sizeZ = nbt.getInteger("sizeZ");

		Blueprint blueprint = new Blueprint(sizeX, sizeY, sizeZ, new UUID(most, least));

		blueprint.name = nbt.getString("name");
		blueprint.creator = nbt.getString("creator");

		blueprint.anchorX = nbt.getInteger("anchorX");
		blueprint.anchorY = nbt.getInteger("anchorY");
		blueprint.anchorZ = nbt.getInteger("anchorZ");

		blueprint.anchorOrientation = ForgeDirection.getOrientation(nbt.getByte("anchorOrientation"));

		NBTTagList blockList = nbt.getTagList("blocks");
		for (int i = 0; i < blockList.tagCount(); i++) {
			NBTTagCompound blockNBT = (NBTTagCompound) blockList.tagAt(i);
			BlockSchematic block = BlockSchematic.readFromNBT(blockNBT);
			blueprint.blocks[block.x][block.y][block.z] = block;
		}
		return blueprint;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
}
