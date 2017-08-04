package buildcraft.builders.filler;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.statement.StatementContext;

public enum FillerStatementContext implements StatementContext<IFillerPattern> {
    CONTEXT_ALL;

    /*
     * All immutable - the filler always shows everything, and for
     * now there aren't enough shapes to require multiple groups.
     */

    private static final List<Group> actualGroup = ImmutableList.of(Group.INSTANCE);
    private static final List<IFillerPattern> possibleValues;

    static {
        possibleValues = new ArrayList<>();
        setupPossible();
    }

    private static void setupPossible() {
        possibleValues.clear();
        for (IFillerPattern pattern : FillerManager.registry.getPatterns()) {
            possibleValues.add(pattern);
        }
    }

    @Override
    public List<Group> getAllPossible() {
        return actualGroup;
    }

    static enum Group implements StatementGroup<IFillerPattern> {
        INSTANCE;

        @Override
        public ISimpleDrawable getSourceIcon() {
            return null;
        }

        @Override
        public List<IFillerPattern> getValues() {
            return possibleValues;
        }
    }
}
