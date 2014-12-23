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
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.core.utils.Utils;

public final class SchematicRegistry implements ISchematicRegistry {

	public static SchematicRegistry INSTANCE = new SchematicRegistry();
	
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
			throw new IllegalArgumentException("Builder: Could not find matching constructor for class " + clazz);
		}
	}
	
	public void registerSchematicBlock(Block block, Class<? extends Schematic> clazz, Object... params) {
		for (Object o: block.getBlockState().getValidStates()) {
			IBlockState state = (IBlockState) o;
			registerSchematicBlock(state, clazz, params);
		}
	}
	
	public void registerSchematicBlock(IBlockState state, Class<? extends Schematic> clazz, Object... params) {
		Block block = state.getBlock();
		if (block == null || Utils.getBlockName(block) == null || "null".equals(Utils.getBlockName(block))) {
			BCLog.logger.warn("Builder: Mod tried to register block '" + (block != null ? block.getClass().getName() : "null") + "' schematic with a null name! Ignoring.");
			return;
		}

		if (schematicBlocks.containsKey(toStringKey(state))) {
			//BCLog.logger.warn("Builder: Block " + Utils.getBlockName(block) + " is already associated with a schematic. Ignoring...");
			return;
		}

		schematicBlocks.put(toStringKey(state), new SchematicConstructor(clazz, params));
	}

	public void registerSchematicEntity(
			Class<? extends Entity> entityClass,
			Class<? extends SchematicEntity> schematicClass, Object... params) {
		if (schematicEntities.containsKey(entityClass)) {
			throw new RuntimeException("Builder: Entity " + entityClass.getName() + " is already associated with a schematic.");
		}
		schematicEntities.put(entityClass, new SchematicConstructor(schematicClass, params));
	}

	public SchematicBlock createSchematicBlock(IBlockState state) {
		SchematicConstructor c = schematicBlocks.get(toStringKey(state));

		if (c == null) {
			return null;
		}
		
		try {
			SchematicBlock s = (SchematicBlock) c.newInstance();
			s.state = state;
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

	public boolean isSupported(IBlockState state) {
		return schematicBlocks.containsKey(toStringKey(state));
	}

	public boolean isAllowedForBuilding(IBlockState state) {
		Block block = state.getBlock();
		String name = Utils.getBlockName(block);
		return isSupported(state) && !blocksForbidden.contains(name) && !modsForbidden.contains(name.split(":", 2)[0]);
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
	
	private String toStringKey(IBlockState state) {
		return (Utils.getBlockName(state.getBlock())) + ":" + state.getBlock().getMetaFromState(state);
	}
}
