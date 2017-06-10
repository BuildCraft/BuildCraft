/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

public interface IExpressionNode {
    default IExpressionNode inline() {
        return this;
    }

    // common expression types

    @FunctionalInterface
    interface INodeDouble extends IExpressionNode {
        double evaluate();

        @Override
        default INodeDouble inline() {
            return this;
        }
    }

    @FunctionalInterface
    interface INodeLong extends IExpressionNode {
        long evaluate();

        @Override
        default INodeLong inline() {
            return this;
        }
    }

    @FunctionalInterface
    interface INodeBoolean extends IExpressionNode {
        boolean evaluate();

        @Override
        default INodeBoolean inline() {
            return this;
        }
    }

    @FunctionalInterface
    interface INodeString extends IExpressionNode {
        String evaluate();

        @Override
        default INodeString inline() {
            return this;
        }
    }
}
