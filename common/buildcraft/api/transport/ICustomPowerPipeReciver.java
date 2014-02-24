package buildcraft.api.transport;

import net.minecraftforge.common.ForgeDirection;

/**
 * 
 * @author Speiger
 *
 */
public interface ICustomPowerPipeReciver
{
	/**
	 * My Custom Power Pipe Function.
	 * This Interface allow People to add their own Custom Pipes without extends their Mod.
	 * If you use this Interface then you have to create your own PowerTransfering System.
	 * This Interface does not allow to use it in Normal Machines.
	 */
	
	
	
	/**
	 * Request function for BC Pipes
	 */
	public float requestEnergy(ForgeDirection from, float amount);
	
	/**
	 * Receiving Function for Custom Pipes. So there is no Energy Loss
	 */
	public float receiveEnergy(ForgeDirection form, float val);
	
	/**
	 * This function let the pipes know if you want to connect or not
	 */
	public boolean canConnect(ForgeDirection from);
	
}
