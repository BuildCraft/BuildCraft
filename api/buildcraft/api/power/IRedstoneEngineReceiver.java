package buildcraft.api.power;

import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyReceiver;

/**
 * Implement this on tiles that you wish to be able to receive Redstone Engine
 * (low-power) energy.
 *
 * Please do not implement it on batteries, pipes or machines which can have
 * their energy extracted from. That could lead to exploits and abuse.
 */
public interface IRedstoneEngineReceiver extends IEnergyReceiver {
	/**
	 * This function is queried on every attempt to receive energy from a
	 * redstone engine as well.
	 * @param side
	 * @return
	 */
	boolean canConnectRedstoneEngine(ForgeDirection side);
}
