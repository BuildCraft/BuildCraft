/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.power;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Engines should implement this interface if they want to support
 * BuildCraft's behaviour of passing energy between engines
 * without using receiveEnergy() (which has other issues).
 */
public interface IEngine {
    /**
     * Returns true if the engine wants to receive power from
     * another engine on this side.
     * @param side
     * @return
     */
    boolean canReceiveFromEngine(ForgeDirection side);

    /**
     * Receives energy from an engine.
     * See {@link cofh.api.energy.IEnergyHandler#receiveEnergy(ForgeDirection, int, boolean)}
     * @param side The side the engine is receiving energy from.
     * @param energy The amount of energy given to the engine.
     * @param simulate True if the energy should not actually be added.
     * @return The amount of energy used by the engine.
     */
    int receiveEnergyFromEngine(ForgeDirection side, int energy, boolean simulate);
}
