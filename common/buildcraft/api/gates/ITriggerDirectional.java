package buildcraft.api.gates;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

// TODO: Merge with ITrigger

/**
 * This interface was added to expand on the existing Trigger interface without breaking the API. At some point when it is safe to break the API, this function
 * should replace the one in ITrigger.
 */
public interface ITriggerDirectional extends ITrigger {

	/**
	 * Return true if the tile given in parameter activates the trigger, given the parameters.
	 */
	public abstract boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter);
}
