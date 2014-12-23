/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Implement and register with the PipeManager if you want to suppress connections from wooden pipes.
 */
public interface IExtractionHandler {

	/**
	 * Can this pipe extract items from the block located at these coordinates?
	 * param extractor can be null
	 */
	boolean canExtractItems(Object extractor, World world, BlockPos pos);

	/**
	 * Can this pipe extract liquids from the block located at these coordinates?
	 * param extractor can be null
	 */
	boolean canExtractFluids(Object extractor, World world, BlockPos pos);
}
