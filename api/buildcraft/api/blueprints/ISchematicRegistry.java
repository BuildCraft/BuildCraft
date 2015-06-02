/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

/** Use this interface to register blocks and entities with the current SchematicRegistry. Use the instance from
 * BuilderAPI.schematicRegistry to register schematics. */
public interface ISchematicRegistry {
	/** Register all of the block's block states to all use the same Schematic class. The params argument is used in the
	 * same way as {@link #registerSchematicBlock(IBlockState, Class, Object[])} */
	void registerSchematicBlock(Block block, Class<? extends Schematic> clazz, Object... params);

	/** Register a block state with the specified Schematic class. The params argument should be the same as the
	 * constructor that you want to invoke. For example, SchematicTreatAs takes a single IBlockState argument in its
	 * constructor, so to register it with a block (for example treat all diamond ore blocks as stone, you would call
	 * <p>
	 * <code>
	 * 	BuilderAPI.schematicRegistry.registerSchematicBlock(Blocks.diamond_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
	 *  </code> */
	void registerSchematicBlock(IBlockState state, Class<? extends Schematic> clazz, Object... params);

	/** Register an entity with the specified schematic. The only different from
	 * {@link #registerSchematicBlock(IBlockState, Class, Object[])} is that the Schematic MUST be an instance of
	 * SchematicEntity. Note that this means that you can technically change blocks to entities in the other methods. */
	void registerSchematicEntity(Class<? extends Entity> entityClass, Class<? extends SchematicEntity> schematicClass, Object... params);

	/** @return <code>True</code> if the block state given has been registered by
	 *         {@link #registerSchematicBlock(Block, Class, Object[])} or
	 *         {@link #registerSchematicBlock(IBlockState, Class, Object[])}. */
	boolean isSupported(IBlockState state);
}
