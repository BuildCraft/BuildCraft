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

public class SchematicFactoryMask extends SchematicFactory <SchematicMask> {

	@Override
	protected SchematicMask loadSchematicFromWorldNBT (NBTTagCompound nbt, MappingRegistry registry) {
		SchematicMask s = new SchematicMask();
		s.readFromNBT(nbt, registry);

		return s;
	}

	@Override
	public void saveSchematicToWorldNBT (NBTTagCompound nbt, SchematicMask object, MappingRegistry registry) {
		super.saveSchematicToWorldNBT(nbt, object, registry);

		object.writeToNBT(nbt, registry);
	}

}
