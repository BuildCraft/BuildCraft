package buildcraft.api.transport;

import net.minecraftforge.common.ForgeDirection;

public interface ICustomPowerPipeProvider
{
	/**
	 * This Interface is the part where your Pipe can be a PowerProvider.
	 * If you want to send energy you have to call the IPowerPipeTile.
	 */
	
	/**
	 * Check function if the Pipe can connect.
	 */
	public boolean canConnect(ForgeDirection from);
}
