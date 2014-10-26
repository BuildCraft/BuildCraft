/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BlockMetaPair;
import buildcraft.api.core.JavaTools;

public class SchematicRegistry implements ISchematicRegistry {

	public static SchematicRegistry INSTANCE = new SchematicRegistry();
	
	private final HashMap<BlockMetaPair, SchematicConstructor> schematicBlocks =
			new HashMap<BlockMetaPair, SchematicConstructor>();

	private final HashMap<Class<? extends Entity>, SchematicConstructor> schematicEntities = new HashMap<Class<? extends Entity>, SchematicConstructor>();

	private final HashSet<String> modsForbidden = new HashSet<String>();
	private final HashSet<String> blocksForbidden = new HashSet<String>();

	private SchematicRegistry() {
	}

	private class SchematicConstructor {
		public final Class<? extends Schematic> clazz;
		public final Object[] params;

		private final Constructor<?> constructor;

		SchematicConstructor(Class<? extends Schematic> clazz, Object[] params) throws IllegalArgumentException {
			this.clazz = clazz;
			this.params = params;
			this.constructor = findConstructor();
		}

		public Schematic newInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException {
			return (Schematic) constructor.newInstance(params);
		}

		private Constructor<?> findConstructor() throws IllegalArgumentException {
			for (Constructor<?> c : clazz.getConstructors()) {
				Class<?>[] typesSignature = c.getParameterTypes();
				if (typesSignature.length != params.length) {
					// non-matching constructor count arguments, skip
					continue;
				}
				boolean valid = true;
				for (int i = 0; i < params.length; i++) {
					if (params[i] == null) {
						// skip checking for null parameters
						continue;
					}
					Class<?> paramClass = params[i].getClass();
					if (!(typesSignature[i].isAssignableFrom(paramClass)
							|| (typesSignature[i] == int.class && paramClass == Integer.class)
							|| (typesSignature[i] == double.class && paramClass == Double.class)
							|| (typesSignature[i] == boolean.class && paramClass == Boolean.class))) {
						// constructor has non assignable parameters skip constructor...
						valid = false;
						break;
					}
				}
				if (!valid) {
					continue;
				}
				return c;
			}
			throw new IllegalArgumentException("Could not find matching constructor for class " + clazz);
		}
	}
	
	public void registerSchematicBlock(Block block, Class<? extends Schematic> clazz, Object... params) {
		registerSchematicBlock(block, OreDictionary.WILDCARD_VALUE, clazz, params);
	}
	
	public void registerSchematicBlock(Block block, int meta, Class<? extends Schematic> clazz, Object... params) {
		if (schematicBlocks.containsKey(new BlockMetaPair(block, meta))) {
			throw new RuntimeException("Block " + Block.blockRegistry.getNameForObject(block) + " is already associated with a schematic.");
		}
		schematicBlocks.put(new BlockMetaPair(block, meta), new SchematicConstructor(clazz, params));
	}

	public void registerSchematicEntity(
			Class<? extends Entity> entityClass,
			Class<? extends SchematicEntity> schematicClass, Object... params) {
		if (schematicEntities.containsKey(entityClass)) {
			throw new RuntimeException("Entity " + entityClass.getName() + " is already associated with a schematic.");
		}
		schematicEntities.put(entityClass, new SchematicConstructor(schematicClass, params));
	}

	public SchematicBlock createSchematicBlock(Block block, int metadata) {
		if (block == Blocks.air) {
			return null;
		}

		BlockMetaPair pairWildcard = new BlockMetaPair(block, OreDictionary.WILDCARD_VALUE);
		BlockMetaPair pair = new BlockMetaPair(block, metadata);

		SchematicConstructor c = null;
		
		if (schematicBlocks.containsKey(pair)) {
			c = schematicBlocks.get(pair);
		} else if (schematicBlocks.containsKey(pairWildcard)) {
			c = schematicBlocks.get(pairWildcard);
		}

		if (c == null) {
			return null;
		}
		
		try {
			SchematicBlock s = (SchematicBlock) c.newInstance();
			s.block = block;
			return s;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		return null;
	}

	public SchematicEntity createSchematicEntity(Class<? extends Entity> entityClass) {
		if (!schematicEntities.containsKey(entityClass)) {
			return null;
		}

		try {
			SchematicConstructor c = schematicEntities.get(entityClass);
			SchematicEntity s = (SchematicEntity) c.newInstance();
			s.entity = entityClass;
			return s;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean isSupported(Block block, int metadata) {
		return schematicBlocks.containsKey(new BlockMetaPair(block, OreDictionary.WILDCARD_VALUE))
				|| schematicBlocks.containsKey(new BlockMetaPair(block, metadata));
	}

	public boolean isAllowedForBuilding(Block block, int metadata) {
		String name = Block.blockRegistry.getNameForObject(block);
		return isSupported(block, metadata) && !blocksForbidden.contains(name) && !modsForbidden.contains(name.split(":", 2)[0]);
	}

	public void readConfiguration(Configuration conf) {
		Property excludedMods = conf.get(Configuration.CATEGORY_GENERAL, "builder.excludedMods", new String[0],
				"mods that should be excluded from the builder.");
		Property excludedBlocks = conf.get(Configuration.CATEGORY_GENERAL, "builder.excludedBlocks", new String[0],
				"blocks that should be excluded from the builder.");

		for (String id : excludedMods.getStringList()) {
			String strippedId = JavaTools.stripSurroundingQuotes(id.trim());

			if (strippedId.length() > 0) {
				modsForbidden.add(strippedId);
			}
		}

		for (String id : excludedBlocks.getStringList()) {
			String strippedId = JavaTools.stripSurroundingQuotes(id.trim());

			if (strippedId.length() > 0) {
				blocksForbidden.add(strippedId);
			}
		}
	}
}
