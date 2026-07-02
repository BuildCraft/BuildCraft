package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.core.statements.StatementParameterItemStackExact;

public abstract class ActionStationInputItems extends BCStatement implements IActionInternal {

    public ActionStationInputItems(String... tags) {
        super(tags);
    }

    @Override
    public int maxParameters() { return 3; }
    @Override
    public int minParameters() { return 1; }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStackExact();
    }
}
