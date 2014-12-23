/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.power;

import net.minecraft.util.EnumFacing;

/**
 * Engines should implement this interface if they want to support
 * BuildCraft's behaviour of passing energy between engines
 * without using receiveEnergy() (which has other issues).
 */
public interface IEngine {
    boolean canReceiveFromEngine(EnumFacing side);
    int receiveEnergyFromEngine(EnumFacing side, int energy, boolean simulate);
}
