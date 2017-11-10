/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;

public class SchematicFactoryMask extends SchematicFactory<SchematicMask> {

	@Override
	protected SchematicMask loadSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry) {
		SchematicMask s = new SchematicMask();
		s.readSchematicFromNBT(nbt, registry);

		return s;
	}

	@Override
	public void saveSchematicToWorldNBT(NBTTagCompound nbt, SchematicMask object, MappingRegistry registry) {
		super.saveSchematicToWorldNBT(nbt, object, registry);

		object.writeSchematicToNBT(nbt, registry);
	}

}
