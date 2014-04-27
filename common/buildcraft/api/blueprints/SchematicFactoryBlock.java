/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;

public class SchematicFactoryBlock extends SchematicFactory<SchematicBlock> {

	@Override
	protected SchematicBlock loadSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry) {
		int blockId = nbt.getInteger("blockId");
		Block b = registry.getBlockForId(blockId);

		if (b == Blocks.air) {
			SchematicBlock s = new SchematicBlock();
			s.meta = 0;
			s.block = Blocks.air;

			return s;
		} else {
			SchematicBlock s = SchematicRegistry.newSchematicBlock(b);

			if (s != null) {
				s.readFromNBT(nbt, registry);
				return s;
			}
		}

		return null;
	}

	@Override
	public void saveSchematicToWorldNBT(NBTTagCompound nbt, SchematicBlock object, MappingRegistry registry) {
		super.saveSchematicToWorldNBT(nbt, object, registry);

		nbt.setInteger("blockId", registry.getIdForBlock(object.block));
		object.writeToNBT(nbt, registry);
	}

}
