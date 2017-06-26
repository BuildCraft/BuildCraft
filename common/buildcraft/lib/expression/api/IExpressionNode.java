/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public interface IExpressionNode {
    default IExpressionNode inline() {
        return this;
    }

    String evaluateAsString();

    // common expression types

    @FunctionalInterface
    interface INodeDouble extends IExpressionNode, DoubleSupplier {
        double evaluate();

        @Override
        default INodeDouble inline() {
            return this;
        }

        @Override
        default String evaluateAsString() {
            return Double.toString(evaluate());
        }

        /** @deprecated As {@link #evaluate()} gives a better description as to the cost. */
        @Override
        default double getAsDouble() {
            return evaluate();
        }
    }

    @FunctionalInterface
    interface INodeLong extends IExpressionNode, LongSupplier {
        long evaluate();

        @Override
        default INodeLong inline() {
            return this;
        }

        @Override
        default String evaluateAsString() {
            return Long.toString(evaluate());
        }

        /** @deprecated As {@link #evaluate()} gives a better description as to the cost. */
        @Override
        default long getAsLong() {
            return evaluate();
        }
    }

    @FunctionalInterface
    interface INodeBoolean extends IExpressionNode, BooleanSupplier {
        boolean evaluate();

        @Override
        default INodeBoolean inline() {
            return this;
        }

        @Override
        default String evaluateAsString() {
            return Boolean.toString(evaluate());
        }

        /** @deprecated As {@link #evaluate()} gives a better description as to the cost. */
        @Override
        default boolean getAsBoolean() {
            return evaluate();
        }

    }

    @FunctionalInterface
    interface INodeString extends IExpressionNode, Supplier<String> {
        String evaluate();

        @Override
        default INodeString inline() {
            return this;
        }

        /** @deprecated As {@link #evaluate()} is a shorter call. */
        @Override
        default String evaluateAsString() {
            return evaluate();
        }

        /** @deprecated As {@link #evaluate()} gives a better description as to the cost. */
        @Override
        default String get() {
            return evaluate();
        }
    }
}
