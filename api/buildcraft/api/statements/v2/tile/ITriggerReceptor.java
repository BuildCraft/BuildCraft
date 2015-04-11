package buildcraft.api.statements.v2.tile;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.statements.v2.IStatementContainer;

/**
 * Implement this interface on a tile entity to intercept triggers.
 */
public interface ITriggerReceptor {
	enum Result {
		TRUE, FALSE, DEFAULT;
	}
	/**
	 * Intercepts a trigger activated on this tile.
	 * @param container The container with the currently executed trigger.
	 * @param side The side of the tile, relative to the container.
	 * @return True or False to override the trigger output, Default otherwise.
	 */
	Result triggerActivated(IStatementContainer container, ForgeDirection side);
}
