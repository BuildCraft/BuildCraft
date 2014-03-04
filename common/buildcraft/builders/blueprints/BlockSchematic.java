/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import buildcraft.api.builder.BlockHandler;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public final class BlockSchematic extends SchematicOld {

	public static BlockSchematic create(NBTTagCompound nbt) {
		return null;
	}

	public static BlockSchematic create(Block block) {
		return new BlockSchematic(block);
	}

	/**
	 * For serializer use only
	 */
	public BlockSchematic() {

	}

	private BlockSchematic(Block block) {
		super(Block.blockRegistry.getIDForObject(block));
	}

	private BlockSchematic(String nbt) {
//		String blockName = nbt.getString("blockName");
		this((Block) null); // TODO: Add block from name code
	}

	@Override
	public BlockHandler getHandler() {
		return BlockHandler.get((Block) Block.blockRegistry.getObjectById(id));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setString("schematicType", "block");
		nbt.setString("blockName", ((Block) Block.blockRegistry.getObjectById(id)).getUnlocalizedName());
	}
}
