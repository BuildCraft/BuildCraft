package buildcraft.lib.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.misc.data.IReference;

public class ParamSlot implements IReference<IStatementParameter> {
    private IStatementParameter param;

    @Override
    public IStatementParameter get() {
        return param;
    }

    /** Sets this parameter DIRECTLY, without updating clients or the server. */
    void setWithoutUpdating(IStatementParameter to) {
        this.param = to;
    }

    @Override
    public void set(IStatementParameter to) {
        setWithoutUpdating(to);
        // TODO: Property updating
    }

    void onSetMain(IStatement statement, int paramIndex) {
        if (param == null) {
            return;
        }
        if (paramIndex > statement.maxParameters()) {
            set(null);
        } else {
            set(param.convertForNewStatement(statement, paramIndex));
        }
    }

    @Override
    public boolean canSet(Object value) {
        return value instanceof IStatementParameter;
    }
}
