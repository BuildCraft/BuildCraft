/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.blueprints;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.Maps;

public abstract class SchematicFactory<S extends Schematic> {

	private static final HashMap<String, SchematicFactory<? extends Schematic>> factories = Maps.newHashMap();

	private static final HashMap<Class<? extends Schematic>, SchematicFactory<?>> schematicToFactory = Maps.newHashMap();

	protected abstract S loadSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry)
			throws MappingNotFoundException;

	public void saveSchematicToWorldNBT (NBTTagCompound nbt, S object, MappingRegistry registry) {
		nbt.setString("factoryID", getClass().getCanonicalName());
	}

	public static Schematic createSchematicFromWorldNBT(NBTTagCompound nbt, MappingRegistry registry)
			throws MappingNotFoundException {
		String factoryName = nbt.getString("factoryID");

		if (factories.containsKey(factoryName)) {
			return factories.get(factoryName).loadSchematicFromWorldNBT(nbt, registry);
		} else {
			return null;
		}
	}

	public static void registerSchematicFactory(Class<? extends Schematic> clas, SchematicFactory<?> factory) {
		schematicToFactory.put(clas, factory);
		factories.put(factory.getClass().getCanonicalName(), factory);
	}

	// Unfortunately we cannot use generics properly as clas.getSuperclass() returns <? super T> instead of 
	// <? extends Schemtic as we want it to>
	@SuppressWarnings("rawtypes")
	public static SchematicFactory getFactory(Class clas) {
		Class<?> superClass = clas.getSuperclass();

		if (schematicToFactory.containsKey(clas)) {
			return  schematicToFactory.get(clas);
		} else if (superClass != null) {
			return getFactory(superClass);
		}
		else {
			return null;
		}
	}

}
