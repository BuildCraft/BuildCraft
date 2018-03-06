/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeFuncGenericToDouble extends NodeFuncGeneric implements INodeFuncDouble {

    protected final INodeDouble node;

    public NodeFuncGenericToDouble(INodeDouble node, Class<?>[] types, IVariableNode[] nodes) {
        super(node, types, nodes);
        this.node = node;
    }

    @Override
    public INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
        return new FuncDouble(popArgs(stack));
    }

    protected class FuncDouble extends Func implements INodeDouble {
        public FuncDouble(IExpressionNode[] argsIn) {
            super(argsIn);
        }

        @Override
        public double evaluate() {
            setupEvaluate(realArgs);
            return node.evaluate();
        }

        @Override
        public INodeDouble inline() {
            IExpressionNode[] newArgs = new IExpressionNode[realArgs.length];
            InlineType type = setupInline(newArgs);
            if (type == InlineType.FULL) {
                setupEvaluate(newArgs);
                return new NodeConstantDouble(node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncDouble(newArgs);
            }
            return this;
        }
    }
}
