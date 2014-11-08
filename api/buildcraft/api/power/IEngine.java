package buildcraft.api.power;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Engines should implement this interface if they want to support
 * BuildCraft's behaviour of passing energy between engines
 * without using receiveEnergy() (which has other issues).
 */
public interface IEngine {
    boolean canReceiveFromEngine(ForgeDirection side);
    int receiveEnergyFromEngine(ForgeDirection side, int energy, boolean simulate);
}
