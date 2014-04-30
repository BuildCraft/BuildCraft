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

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;

public class SchematicRegistry {

	private static class SchematicConstructor {
		Class <? extends SchematicEntity> clas;
		Object [] params;
	}

	private static final HashMap <Block, SchematicConstructor> schematicBlocks =
			new HashMap<Block, SchematicConstructor>();

	private static final HashMap <Class <? extends Entity>, SchematicConstructor> schematicEntities =
			new HashMap<Class <? extends Entity>, SchematicConstructor>();

	public static void registerSchematicBlock (Block block, Class clas, Object ... params) {
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
				registerSchematicBlock(block, SchematicTile.class);
			} else {
				registerSchematicBlock(block, SchematicBlock.class);
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

	public static SchematicEntity newSchematicEntity (Class <? extends Entity> entityClass) {
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

}
