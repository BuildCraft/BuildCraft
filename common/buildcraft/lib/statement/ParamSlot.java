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

    @Override
    public void set(IStatementParameter to) {
        param = to;
    }

    @Override
    public boolean canSet(Object value) {
        return value instanceof IStatementParameter;
    }

    void onSetMain(IStatement statement, int paramIndex) {
        if (param == null) {
            if (paramIndex < statement.maxParameters()) {
                set(statement.createParameter(paramIndex));
            }
            return;
        }
        if (paramIndex > statement.maxParameters()) {
            set(null);
        } else {
            set(param.convertForNewStatement(statement, paramIndex));
        }
    }
}
