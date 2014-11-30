/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.tiles;

/**
 * This interface should be implemented by Tile Entities
 * which have an internal heat value.
 */
public interface IHeatable {
    /**
     * @return The minimum heat value, in degrees.
     */
    double getMinHeatValue();

    /**
     * @return The preferred heat value, in degrees.
     */
    double getIdealHeatValue();

    /**
     * @return The maxmimum heat value, in degrees.
     */
    double getMaxHeatValue();

    /**
     * @return The current heat value, in degrees.
     */
    double getCurrentHeatValue();

    /**
     * Set the heat of the tile.
     * @param value Heat value, in degrees.
     * @return The heat the tile has after the set.
     */
    double setHeatValue(double value);
}
