/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.api.core.JavaTools;

public final class SchematicRegistry {

	public static double BREAK_ENERGY = 10;
	public static final double BUILD_ENERGY = 20;

	private static final HashSet<Block> explicitSchematicBlocks = new HashSet<Block>();

	private static final HashMap<Block, SchematicConstructor> schematicBlocks =
			new HashMap<Block, SchematicConstructor>();

	private static final HashMap<Class<? extends Entity>, SchematicConstructor> schematicEntities = new HashMap<Class<? extends Entity>, SchematicConstructor>();

	private static final HashSet<String> modsSupporting = new HashSet<String>();
	private static final HashSet<String> modsForbidden = new HashSet<String>();
	private static final HashSet<String> blocksForbidden = new HashSet<String>();

	/**
	 * Deactivate constructor
	 */
	private SchematicRegistry() {
	}

	private static class SchematicConstructor {
		Class<? extends SchematicEntity> clas;
		Object [] params;
	}

	public static void registerSchematicBlock (Block block, Class clas, Object ... params) {
		explicitSchematicBlocks.add(block);
		internalRegisterSchematicBlock(block, clas, params);
	}

	private static void internalRegisterSchematicBlock (Block block, Class clas, Object ... params) {
		if (schematicBlocks.containsKey(block)) {
			throw new RuntimeException("Block " + Block.blockRegistry.getNameForObject(block)
					+ " is already associated with a schematic.");
		}

		SchematicConstructor c = new SchematicConstructor ();
		c.clas = clas;
		c.params = params;

		schematicBlocks.put(block, c);
	}

	public static void registerSchematicEntity(
			Class<? extends Entity> entityClass,
			Class<? extends SchematicEntity> schematicClass, Object... params) {

		SchematicConstructor c = new SchematicConstructor ();
		c.clas = schematicClass;
		c.params = params;

		schematicEntities.put(entityClass, c);
	}

	public static SchematicBlock newSchematicBlock (Block block) {
		if (block == Blocks.air) {
			return null;
		}

		if (!schematicBlocks.containsKey(block)) {
			if (block instanceof ITileEntityProvider) {
				internalRegisterSchematicBlock(block, SchematicTile.class);
			} else {
				internalRegisterSchematicBlock(block, SchematicBlock.class);
			}
		}

		try {
			SchematicConstructor c = schematicBlocks.get(block);
			SchematicBlock s = (SchematicBlock) c.clas.getConstructors() [0].newInstance(c.params);
			s.block = block;
			return s;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static SchematicEntity newSchematicEntity(Class<? extends Entity> entityClass) {
		if (!schematicEntities.containsKey(entityClass)) {
			return null;
		}

		try {
			SchematicConstructor c = schematicEntities.get(entityClass);
			SchematicEntity s = (SchematicEntity) c.clas.getConstructors() [0].newInstance(c.params);
			s.entity = entityClass;
			return s;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void declareBlueprintSupport (String modid) {
		modsSupporting.add(modid);
	}

	public static boolean isExplicitlySupported (Block block) {
		String modid = Block.blockRegistry.getNameForObject(block).split(":") [0];

		return explicitSchematicBlocks.contains(block) || modsSupporting.contains(modid);
	}

	public static boolean isAllowedForBuilding (Block block) {
		String name = Block.blockRegistry.getNameForObject(block);
		String modid = name.split(":") [0];

		return !modsForbidden.contains(modid) && !blocksForbidden.contains(name);
	}

	public static void readConfiguration (Configuration conf) {
		Property excludedMods = conf.get(Configuration.CATEGORY_GENERAL, "builder.excludedMods", new String [0],
				"mods that should be excluded from the builder.");
		Property excludedBlocks = conf.get(Configuration.CATEGORY_GENERAL, "builder.excludedBlocks", new String [0],
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

	static {
		modsSupporting.add ("minecraft");
	}
}
