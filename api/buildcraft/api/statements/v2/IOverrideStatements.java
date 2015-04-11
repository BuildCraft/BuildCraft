package buildcraft.api.statements.v2;

import java.util.List;

public interface IOverrideStatements {
	void overrideTriggers(List<Trigger> triggers);
	void overrideActions(List<Action> actions);
}
