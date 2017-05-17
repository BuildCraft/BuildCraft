package buildcraft.builders.gui;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatement;

import buildcraft.lib.gui.statement.StatementWrapper;

public class FillerWrapper extends StatementWrapper implements IFillerPattern {

    public FillerWrapper(IStatement delegate) {
        super(delegate, EnumPipePart.CENTER);
    }

    @Override
    public FillerWrapper[] getPossible() {
        IStatement[] possible = delegate.getPossible();
        FillerWrapper[] real = new FillerWrapper[possible.length];
        for (int i = 0; i < possible.length; i++) {
            real[i] = new FillerWrapper(possible[i]);
        }
        return real;
    }

}
