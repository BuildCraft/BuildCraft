package buildcraft.api.statements.v2.tile;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.statements.v2.IStatementContainer;

/**
 * Implement this interface on a tile entity to intercept actions.
 */
public interface IActionReceptor {
	/**
	 * Intercepts an action activated on this tile.
	 * @param container The container with the currently executed action.
	 * @param side The side of the tile, relative to the container.
	 * @return True to cancel the actual action code, false otherwise.
	 */
	boolean actionActivated(IStatementContainer container, ForgeDirection side);
}
