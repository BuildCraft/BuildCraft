/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.blueprints;

import net.minecraft.world.World;
import buildcraft.api.core.IBox;
import buildcraft.api.core.Position;

/**
 * This interface provide contextual information when building or initializing
 * blueprint slots.
 */
public interface IBuilderContext {

	Position rotatePositionLeft(Position pos);

	IBox surroundingBox();

	World world();

	MappingRegistry getMappingRegistry();
}
