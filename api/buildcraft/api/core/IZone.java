/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import java.util.Random;
import net.minecraft.util.BlockPos;

public interface IZone {

	double distanceTo(BlockPos pos);

	boolean contains(double x, double y, double z);

	BlockPos getRandomBlockPos(Random rand);

}
