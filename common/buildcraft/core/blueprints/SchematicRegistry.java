/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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
import net.minecraft.entity.Entity;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;

public final class SchematicRegistry implements ISchematicRegistry {

	public static SchematicRegistry INSTANCE = new SchematicRegistry();
	private static final HashMap<Class<? extends Schematic>, Constructor<?>> emptyConstructorMap = new HashMap<Class<? extends Schematic>, Constructor<?>>();

	public final HashMap<String, SchematicConstructor> schematicBlocks =
			new HashMap<String, SchematicConstructor>();

	public final HashMap<Class<? extends Entity>, SchematicConstructor> schematicEntities = new HashMap<Class<? extends Entity>, SchematicConstructor>();

	private final HashSet<String> modsForbidden = new HashSet<String>();
	private final HashSet<String> blocksForbidden = new HashSet<String>();

	private SchematicRegistry() {
	}

	public class SchematicConstructor {
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
			if (params.length == 0 && emptyConstructorMap.containsKey(clazz)) {
				return emptyConstructorMap.get(clazz);
			}

			for (Constructor<?> c : clazz.getConstructors()) {
				if (c == null) {
					continue;
				}
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
				if (params.length == 0) {
					emptyConstructorMap.put(clazz, c);
				}
				return c;
			}
			throw new IllegalArgumentException("Builder: Could not find matching constructor for class " + clazz);
		}
	}

	public void registerSchematicBlock(Block block, Class<? extends Schematic> clazz, Object... params) {
		for (int i = 0; i < 16; i++) {
			registerSchematicBlock(block, i, clazz, params);
		}
	}

	public void registerSchematicBlock(Block block, int meta, Class<? extends Schematic> clazz, Object... params) {
		if (block == null || Block.blockRegistry.getNameForObject(block) == null || "null".equals(Block.blockRegistry.getNameForObject(block))) {
			BCLog.logger.warn("Builder: Mod tried to register block '" + (block != null ? block.getClass().getName() : "null") + "' schematic with a null name! Ignoring.");
			return;
		}
		if (schematicBlocks.containsKey(toStringKey(block, meta))) {
			throw new RuntimeException("Builder: Block " + Block.blockRegistry.getNameForObject(block) + " is already associated with a schematic.");
		}

		schematicBlocks.put(toStringKey(block, meta), new SchematicConstructor(clazz, params));
	}

	public void registerSchematicEntity(
			Class<? extends Entity> entityClass,
			Class<? extends SchematicEntity> schematicClass, Object... params) {
		if (schematicEntities.containsKey(entityClass)) {
			throw new RuntimeException("Builder: Entity " + entityClass.getName() + " is already associated with a schematic.");
		}
		schematicEntities.put(entityClass, new SchematicConstructor(schematicClass, params));
	}

	public SchematicBlock createSchematicBlock(Block block, int metadata) {
		if (block == null || metadata < 0 || metadata >= 16) {
			return null;
		}

		SchematicConstructor c = schematicBlocks.get(toStringKey(block, metadata));

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
		return schematicBlocks.containsKey(toStringKey(block, metadata));
	}

	public boolean isAllowedForBuilding(Block block, int metadata) {
		String name = Block.blockRegistry.getNameForObject(block);
		return isSupported(block, metadata) && !blocksForbidden.contains(name) && !modsForbidden.contains(name.split(":", 2)[0]);
	}

	public void readConfiguration(Configuration conf) {
		Property excludedMods = conf.get("blueprints", "excludedMods", new String[0],
				"mods that should be excluded from the builder.").setLanguageKey("config.blueprints.excludedMods").setRequiresMcRestart(true);
		Property excludedBlocks = conf.get("blueprints", "excludedBlocks", new String[0],
				"blocks that should be excluded from the builder.").setLanguageKey("config.blueprints.excludedBlocks").setRequiresMcRestart(true);

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

	private String toStringKey(Block block, int meta) {
		return Block.blockRegistry.getNameForObject(block) + ":" + meta;
	}
}
