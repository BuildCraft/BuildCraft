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

public class SchematicFactoryEntity extends SchematicFactory <SchematicEntity> {

	@Override
	protected SchematicEntity loadSchematicFromWorldNBT (NBTTagCompound nbt, MappingRegistry registry) {
		int entityId = nbt.getInteger("entityId");
		SchematicEntity s = SchematicRegistry.newSchematicEntity(registry.getEntityForId(entityId));

		if (s != null) {
			s.readFromNBT(nbt, registry);
		} else {
			return null;
		}

		return s;
	}

	@Override
	public void saveSchematicToWorldNBT (NBTTagCompound nbt, SchematicEntity object, MappingRegistry registry) {
		super.saveSchematicToWorldNBT(nbt, object, registry);

		nbt.setInteger("entityId", registry.getIdForEntity(object.entity));
		object.writeToNBT(nbt, registry);
	}

}
