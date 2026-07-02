package ct.buildcraft.builders.filler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.filler.FillerManager;
import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.builders.BCBuildersStatements;
import ct.buildcraft.builders.snapshot.pattern.PatternShape2d;
import ct.buildcraft.lib.gui.ISimpleDrawable;
import ct.buildcraft.lib.statement.StatementContext;

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
        if (Group.DEFAULT.patterns.remove(BCBuildersStatements.PATTERN_NONE)) {
            Group.DEFAULT.patterns.add(0, BCBuildersStatements.PATTERN_NONE);
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
