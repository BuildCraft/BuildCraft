/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeFuncGenericToObject<T> extends NodeFuncGeneric implements INodeFuncObject<T> {

    protected final INodeObject<T> node;

    public NodeFuncGenericToObject(INodeObject<T> node, Class<?>[] types, IVariableNode[] nodes) {
        super(node, types, nodes);
        this.node = node;
    }

    @Override
    public INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
        return new FuncObject(popArgs(stack));
    }

    @Override
    public Class<T> getType() {
        return node.getType();
    }

    protected class FuncObject extends Func implements INodeObject<T> {
        public FuncObject(IExpressionNode[] argsIn) {
            super(argsIn);
        }

        @Override
        public Class<T> getType() {
            return node.getType();
        }

        @Override
        public T evaluate() {
            setupEvaluate(realArgs);
            return node.evaluate();
        }

        @Override
        public INodeObject<T> inline() {
            IExpressionNode[] newArgs = new IExpressionNode[realArgs.length];
            InlineType type = setupInline(newArgs);
            if (type == InlineType.FULL) {
                setupEvaluate(newArgs);
                return new NodeConstantObject<>(getType(), node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncObject(newArgs);
            }
            return this;
        }
    }
}
