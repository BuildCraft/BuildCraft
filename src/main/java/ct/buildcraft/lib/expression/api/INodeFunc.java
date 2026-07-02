/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.api;

import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;

public interface INodeFunc {
    /** Pops values off of the stack to create an expression node. Note that this *must* operate without side effects,
     * as the internal compiler will first test this method with a dummy INodeStack to see if the number of arguments
     * matches. */
    IExpressionNode getNode(INodeStack stack) throws InvalidExpressionException;

    public interface INodeFuncLong extends INodeFunc {
        @Override
        INodeLong getNode(INodeStack stack) throws InvalidExpressionException;
    }

    public interface INodeFuncDouble extends INodeFunc {
        @Override
        INodeDouble getNode(INodeStack stack) throws InvalidExpressionException;
    }

    public interface INodeFuncBoolean extends INodeFunc {
        @Override
        INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException;
    }

    public interface INodeFuncObject<T> extends INodeFunc {
        @Override
        INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException;

        Class<T> getType();
    }
}
