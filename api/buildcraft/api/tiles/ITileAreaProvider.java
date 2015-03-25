package buildcraft.api.tiles;

import buildcraft.api.core.IAreaProvider;

/**
 * Used for more fine-grained control of whether or not a machine connects
 * to the provider here.
 */
public interface ITileAreaProvider extends IAreaProvider {
	boolean isValidFromLocation(int x, int y, int z);
}
