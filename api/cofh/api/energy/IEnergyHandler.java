package cofh.api.energy;

import net.minecraft.util.EnumFacing;

/**
 * Implement this interface on Tile Entities which should handle energy, generally storing it in one or more internal {@link IEnergyStorage} objects.
 * 
 * A reference implementation is provided {@link TileEnergyHandler}.
 * 
 * @author King Lemming
 * 
 */
public interface IEnergyHandler extends IEnergyConnection {

	/**
	 * Add energy to an IEnergyHandler, internal distribution is left entirely to the IEnergyHandler.
	 * 
	 * @param from
	 *            Orientation the energy is received from.
	 * @param maxReceive
	 *            Maximum amount of energy to receive.
	 * @param simulate
	 *            If TRUE, the charge will only be simulated.
	 * @return Amount of energy that was (or would have been, if simulated) received.
	 */
	int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate);

	/**
	 * Remove energy from an IEnergyHandler, internal distribution is left entirely to the IEnergyHandler.
	 * 
	 * @param from
	 *            Orientation the energy is extracted from.
	 * @param maxExtract
	 *            Maximum amount of energy to extract.
	 * @param simulate
	 *            If TRUE, the extraction will only be simulated.
	 * @return Amount of energy that was (or would have been, if simulated) extracted.
	 */
	int extractEnergy(EnumFacing from, int maxExtract, boolean simulate);

	/**
	 * Returns the amount of energy currently stored.
	 */
	int getEnergyStored(EnumFacing from);

	/**
	 * Returns the maximum amount of energy that can be stored.
	 */
	int getMaxEnergyStored(EnumFacing from);

}
