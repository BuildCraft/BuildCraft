package buildcraft.lib.expression.info;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

import gnu.trove.set.TDoubleSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TDoubleHashSet;
import gnu.trove.set.hash.TLongHashSet;

public abstract class VariableInfo<N extends IVariableNode> {
    public final N node;
    private final NodeType nodeType;

    @Nonnull
    public CacheType cacheType = CacheType.NEVER;

    /** If true then the sets containing the possible values are full sets. */
    public boolean setIsComplete = false;

    public VariableInfo(N node) {
        this.node = node;
        nodeType = NodeType.getType(node);
    }

    public abstract Collection<?> getPossibleValues();

    public abstract boolean shouldCacheCurrentValue();

    public enum CacheType {
        NEVER,
        MATCHES_EXP,
        IN_SET,
        ALWAYS;
    }

    public static class VariableInfoString extends VariableInfo<NodeVariableString> {
        public final Set<String> possibleValues = new HashSet<>();
        public Predicate<String> shouldCacheFunc = possibleValues::contains;

        public VariableInfoString(NodeVariableString node) {
            super(node);
        }

        @Override
        public Collection<?> getPossibleValues() {
            return possibleValues;
        }

        @Override
        public boolean shouldCacheCurrentValue() {
            switch (cacheType) {
                case NEVER:
                    return false;
                case MATCHES_EXP:
                    return shouldCacheFunc.test(node.value);
                case IN_SET:
                    return possibleValues.contains(node.value);
                case ALWAYS:
                    return true;
                default:
                    throw new IllegalStateException("Unknown CacheType " + cacheType);
            }
        }
    }

    public static class VariableInfoLong extends VariableInfo<NodeVariableLong> {
        public final TLongSet possibleValues = new TLongHashSet();
        public LongPredicate shouldCacheFunc = possibleValues::contains;

        public VariableInfoLong(NodeVariableLong node) {
            super(node);
        }

        @Override
        public Collection<Long> getPossibleValues() {
            return Arrays.stream(possibleValues.toArray()).boxed().collect(Collectors.toList());
        }

        @Override
        public boolean shouldCacheCurrentValue() {
            switch (cacheType) {
                case NEVER:
                    return false;
                case MATCHES_EXP:
                    return shouldCacheFunc.test(node.value);
                case IN_SET:
                    return possibleValues.contains(node.value);
                case ALWAYS:
                    return true;
                default:
                    throw new IllegalStateException("Unknown CacheType " + cacheType);
            }
        }
    }

    public static class VariableInfoDouble extends VariableInfo<NodeVariableDouble> {
        public final TDoubleSet possibleValues = new TDoubleHashSet();
        public DoublePredicate shouldCacheFunc = possibleValues::contains;

        public VariableInfoDouble(NodeVariableDouble node) {
            super(node);
        }

        @Override
        public Collection<Double> getPossibleValues() {
            return Arrays.stream(possibleValues.toArray()).boxed().collect(Collectors.toList());
        }

        @Override
        public boolean shouldCacheCurrentValue() {
            switch (cacheType) {
                case NEVER:
                    return false;
                case MATCHES_EXP:
                    return shouldCacheFunc.test(node.value);
                case IN_SET:
                    return possibleValues.contains(node.value);
                case ALWAYS:
                    return true;
                default:
                    throw new IllegalStateException("Unknown CacheType " + cacheType);
            }
        }
    }
}
