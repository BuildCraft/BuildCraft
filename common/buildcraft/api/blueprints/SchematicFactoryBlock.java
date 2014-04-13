/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import net.minecraft.nbt.NBTTagCompound;

public class SchematicFactoryBlock extends SchematicFactory <SchematicBlock> {

	@Override
	protected SchematicBlock loadSchematicFromWorldNBT (NBTTagCompound nbt, MappingRegistry registry) {
		int blockId = nbt.getInteger("blockId");
		SchematicBlock s = SchematicRegistry.newSchematicBlock(registry.getBlockForId(blockId));

		if (s != null) {
			s.readFromNBT(nbt, registry);
		} else {
			return null;
		}

		return s;
	}

	@Override
	public void saveSchematicToWorldNBT (NBTTagCompound nbt, SchematicBlock object, MappingRegistry registry) {
		super.saveSchematicToWorldNBT(nbt, object, registry);

		nbt.setInteger("blockId", registry.getIdForBlock(object.block));
		object.writeToNBT(nbt, registry);
	}

}
