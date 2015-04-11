package buildcraft.api.statements.v2.tile;

import java.util.List;
import buildcraft.api.statements.v2.Action;
import buildcraft.api.statements.v2.Trigger;

/**
 * Implement this interface on a tile to edit the trigger/action lists
 * as they are being created.
 */
public interface IOverrideStatements {
	void overrideTriggers(List<Trigger> triggers);
	void overrideActions(List<Action> actions);
}
