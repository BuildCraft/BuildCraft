/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

public interface IIterableAlgorithm {

    /** Iterates this algorithm. What this actually means is up to the algorithm itself, but usually only 1000
     * iterations will be called, so balance around that.
     * 
     * @return True if it has completed */
    boolean iterate();

}
