/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import ct.buildcraft.lib.expression.api.IVariableNode;
import ct.buildcraft.lib.expression.node.value.NodeVariableBoolean;
import ct.buildcraft.lib.expression.node.value.NodeVariableDouble;
import ct.buildcraft.lib.expression.node.value.NodeVariableLong;
import ct.buildcraft.lib.expression.node.value.NodeVariableObject;

public abstract class VariableInfo<N extends IVariableNode> {
    public final N node;

    public CacheType cacheType = CacheType.NEVER;

    /** If true then the sets containing the possible values are full sets. */
    public boolean setIsComplete = false;

    public VariableInfo(N node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return node.toString();
    }

    public abstract Collection<?> getPossibleValues();

    public abstract boolean shouldCacheCurrentValue();

    /** @return A unique ordinal identifying the current value, if it is contained within the possibleValues set, or -1
     *         if its not in the set. Note that this value does NOT have to match the index of the value in the set. */
    public abstract int getCurrentOrdinal();

    public enum CacheType {
        NEVER,
        MATCHES_EXP,
        IN_SET,
        ALWAYS
    }

    public static class VariableInfoObject<T> extends VariableInfo<NodeVariableObject<T>> {
        public final List<T> possibleValues = new ArrayList<>();
        public Predicate<T> shouldCacheFunc = possibleValues::contains;

        public VariableInfoObject(NodeVariableObject<T> node) {
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

        @Override
        public int getCurrentOrdinal() {
            return possibleValues.indexOf(node.value);
        }
    }

    public static class VariableInfoLong extends VariableInfo<NodeVariableLong> {
        public final List<Long> possibleValues = new ArrayList<>();
        public LongPredicate shouldCacheFunc = possibleValues::contains;

        public VariableInfoLong(NodeVariableLong node) {
            super(node);
        }

        @Override
        public Collection<Long> getPossibleValues() {
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

        @Override
        public int getCurrentOrdinal() {
            return possibleValues.indexOf(node.value);
        }
    }

    public static class VariableInfoDouble extends VariableInfo<NodeVariableDouble> {
        public final List<Double> possibleValues = new ArrayList<>();
        public DoublePredicate shouldCacheFunc = possibleValues::contains;

        public VariableInfoDouble(NodeVariableDouble node) {
            super(node);
        }

        @Override
        public Collection<Double> getPossibleValues() {
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

        @Override
        public int getCurrentOrdinal() {
            return possibleValues.indexOf(node.value);
        }
    }

    public static class VariableInfoBoolean extends VariableInfo<NodeVariableBoolean> {
        public enum BooleanPossibilities {
            FALSE(Boolean.FALSE),
            TRUE(Boolean.TRUE),
            FALSE_TRUE(Boolean.FALSE, Boolean.TRUE);

            public final Collection<Boolean> possible;

            BooleanPossibilities(Boolean... possible) {
                this.possible = Arrays.asList(possible);
            }
        }

        public BooleanPossibilities possibleValues = BooleanPossibilities.FALSE_TRUE;

        public VariableInfoBoolean(NodeVariableBoolean node) {
            super(node);
            cacheType = CacheType.ALWAYS;
            setIsComplete = true;
        }

        @Override
        public Collection<Boolean> getPossibleValues() {
            return possibleValues.possible;
        }

        @Override
        public boolean shouldCacheCurrentValue() {
            switch (cacheType) {
                case NEVER:
                    return false;
                case MATCHES_EXP:
                case IN_SET:
                    switch (possibleValues) {
                        case FALSE:
                            return !node.value;
                        case TRUE:
                            return node.value;
                        default:
                            return true;
                    }
                case ALWAYS:
                    return true;
                default:
                    throw new IllegalStateException("Unknown CacheType " + cacheType);
            }
        }

        @Override
        public int getCurrentOrdinal() {
            boolean current = node.value;
            switch (possibleValues) {
                case FALSE: {
                    return current ? -1 : 0;
                }
                case TRUE: {
                    return current ? 0 : -1;
                }
                default:
                case FALSE_TRUE: {
                    return current ? 1 : 0;
                }
            }
        }
    }
}
