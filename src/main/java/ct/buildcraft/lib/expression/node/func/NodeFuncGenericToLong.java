/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.func;

import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.IVariableNode;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncGenericToLong extends NodeFuncGeneric implements INodeFuncLong {

    private final INodeLong node;

    public NodeFuncGenericToLong(INodeLong node, Class<?>[] types, IVariableNode[] nodes) {
        super(node, types, nodes);
        this.node = node;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return new FuncLong(popArgs(stack));
    }

    private class FuncLong extends Func implements INodeLong {
        public FuncLong(IExpressionNode[] argsIn) {
            super(argsIn);
        }

        @Override
        public long evaluate() {
            setupEvaluate(realArgs);
            return node.evaluate();
        }

        @Override
        public INodeLong inline() {
            IExpressionNode[] newArgs = new IExpressionNode[realArgs.length];
            InlineType type = setupInline(newArgs);
            if (type == InlineType.FULL) {
                setupEvaluate(newArgs);
                return new NodeConstantLong(node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncLong(newArgs);
            }
            return this;
        }
    }
}
