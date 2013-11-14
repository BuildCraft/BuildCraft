package buildcraft.transport;

import net.minecraftforge.common.ForgeDirection;

public interface IPipeConnectionForced {
	
	/**
	 * Allows you to block connection overrides.
	 * 
	 * @param with
	 * @return TRUE to block an override. FALSE to allow overrides.
	*/
	public boolean ignoreConnectionOverrides(ForgeDirection with);
}
