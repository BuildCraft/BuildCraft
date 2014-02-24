package buildcraft.api.transport;

import net.minecraftforge.common.ForgeDirection;

public interface IPowerPipeTile
{
	/**
	 * This Interface is only for BC Pipes. DO NOT IMPLEMENT IT TO YOUR CLASS.
	 * Check if the IPipeTile Type is a PowerType and try to cast the Tile into this interface here.
	 */
	
	
	/** If Pipe recive Any Power **/
	public float reciveEnergy(ForgeDirection from, float val);
	
	
	/** If The Pipe Request any Power **/
	public float requestEnergy(ForgeDirection from);
	
}
