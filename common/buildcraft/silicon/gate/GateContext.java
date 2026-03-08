package buildcraft.silicon.gate;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.statement.StatementContext;

import java.util.List;

public class GateContext<T extends IStatement> implements StatementContext<T> {

    public final List<GateGroup<T>> groups;

    public GateContext(List<GateGroup<T>> groups) {
        this.groups = groups;
    }

    @Override
    public List<? extends StatementGroup<T>> getAllPossible() {
        return groups;
    }

    public static class GateGroup<T extends IStatement> implements StatementGroup<T> {
        public final EnumPipePart part;
        public final List<T> statements;

        public GateGroup(EnumPipePart part, List<T> statements) {
            this.part = part;
            this.statements = statements;
        }

        @Override
        public List<T> getValues() {
            return statements;
        }

        @Override
        public ISimpleDrawable getSourceIcon() {
            return null;
        }

        @Override
        public int getLedgerColour() {
            if (part == EnumPipePart.CENTER) {
                return 0;
            }
            return ColourUtil.getColourForSide(part.face);
        }
    }
}
