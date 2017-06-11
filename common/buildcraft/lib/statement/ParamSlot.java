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
        // TODO: Property updating
    }

    void onSetMain(IStatement statement, int paramIndex) {
        if (param == null) {
            return;
        }
        if (paramIndex > statement.maxParameters()) {
            set(null);
            return;
        }
        IStatementParameter newParam = param.convertForNewStatement(statement, paramIndex);
        set(newParam);
    }

    @Override
    public boolean canSet(Object value) {
        return value instanceof IStatementParameter;
    }
}
