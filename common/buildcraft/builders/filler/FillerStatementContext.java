package buildcraft.builders.filler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.statement.StatementContext;

import buildcraft.core.BCCoreStatements;
import buildcraft.core.builders.patterns.PatternShape2d;

public enum FillerStatementContext implements StatementContext<IFillerPattern> {
    CONTEXT_ALL;

    private static final List<Group> groups = ImmutableList.copyOf(Group.values());

    static {
        setupPossible();
    }

    public static void setupPossible() {
        for (Group group : Group.values()) {
            group.patterns.clear();
        }
        for (IFillerPattern pattern : FillerManager.registry.getPatterns()) {
            // TODO (AlexIIL): 8.1.x: add support for other groups
            if (pattern instanceof PatternShape2d) {
                Group.SHAPES_2D.patterns.add(pattern);
            } else {
                Group.DEFAULT.patterns.add(pattern);
            }
        }
        for (Group group : Group.values()) {
            group.patterns.sort(Comparator.comparing(IFillerPattern::getUniqueTag));
        }
        if (Group.DEFAULT.patterns.remove(BCCoreStatements.PATTERN_NONE)) {
            Group.DEFAULT.patterns.add(0, BCCoreStatements.PATTERN_NONE);
        }
    }

    @Override
    public List<Group> getAllPossible() {
        return groups;
    }

    public enum Group implements StatementGroup<IFillerPattern> {
        DEFAULT,
        SHAPES_2D;

        final List<IFillerPattern> patterns = new ArrayList<>();

        @Override
        public ISimpleDrawable getSourceIcon() {
            return null;
        }

        @Override
        public List<IFillerPattern> getValues() {
            return patterns;
        }
    }
}
