package buildcraft.builders.gui;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.statement.StatementWrapper;

public class FillerWrapper extends StatementWrapper implements IFillerPattern {

    public FillerWrapper(IFillerPattern delegate) {
        super(delegate, EnumPipePart.CENTER);
    }

    public IFillerPattern getDelegate() {
        return (IFillerPattern) delegate;
    }

    @Override
    public FillerWrapper[] getPossible() {
        IFillerPattern[] possible = getDelegate().getPossible();
        FillerWrapper[] real = new FillerWrapper[possible.length];
        for (int i = 0; i < possible.length; i++) {
            real[i] = new FillerWrapper(possible[i]);
        }
        return real;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        return getDelegate().createTemplate(filler, params);
    }
}
