/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.core.statements;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * A tile entity implementing this interface will be able to prevent BuildCraft from
 * adding default triggers.
 *
 * This does not block other statement providers from adding triggers or actions.
 * See IOverrideDefaultStatements for a more aggressive approach.
 */
public interface IBlockDefaultTriggers {
	boolean blockInventoryTriggers(ForgeDirection side);
	boolean blockFluidHandlerTriggers(ForgeDirection side);
}
