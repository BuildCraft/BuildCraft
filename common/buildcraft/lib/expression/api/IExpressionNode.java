/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

public interface IExpressionNode {
    IExpressionNode inline();

    // common expression types

    interface INodeDouble extends IExpressionNode {
        double evaluate();

        @Override
        INodeDouble inline();
    }

    interface INodeLong extends IExpressionNode {
        long evaluate();

        @Override
        INodeLong inline();
    }

    interface INodeBoolean extends IExpressionNode {
        boolean evaluate();

        @Override
        INodeBoolean inline();
    }

    interface INodeString extends IExpressionNode {
        String evaluate();

        @Override
        INodeString inline();
    }
}
