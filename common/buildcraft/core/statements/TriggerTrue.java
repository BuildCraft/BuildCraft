package buildcraft.core.statements;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

public class TriggerTrue extends BCStatement implements ITriggerInternal {

    public TriggerTrue() {
        super("buildcraftcore:trigger.true");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
        return true;
    }
}
