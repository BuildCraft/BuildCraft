/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.power;

public interface ILaserTarget {

	/**
	 * Returns true if the target currently needs power. For example, if the Advanced
	 * Crafting Table has work to do.
	 *
	 * @return true if needs power
	 */
	boolean requiresLaserEnergy();

	/**
	 * Transfers energy from the laser to the target.
	 *
	 * @param energy
	 */
	void receiveLaserEnergy(int energy);

	/**
	 * Return true if the Tile Entity object is no longer a valid target. For
	 * example, if its been invalidated.
	 *
	 * @return true if no longer a valid target object
	 */
	boolean isInvalidTarget();

	double getXCoord();

	double getYCoord();

	double getZCoord();
}
