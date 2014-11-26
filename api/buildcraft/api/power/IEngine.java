/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
