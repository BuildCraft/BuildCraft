/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression.node.func;

import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.IVariableNode;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeFuncGenericToBoolean extends NodeFuncGeneric implements INodeFuncBoolean {

    protected final INodeBoolean node;

    public NodeFuncGenericToBoolean(INodeBoolean node, Class<?>[] types, IVariableNode[] nodes) {
        super(node, types, nodes);
        this.node = node;
    }

    @Override
    public INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
        return new FuncBoolean(popArgs(stack));
    }

    protected class FuncBoolean extends Func implements INodeBoolean {

        public FuncBoolean(IExpressionNode[] argsIn) {
            super(argsIn);
        }

        @Override
        public boolean evaluate() {
            setupEvaluate(realArgs);
            return node.evaluate();
        }

        @Override
        public INodeBoolean inline() {
            IExpressionNode[] newArgs = new IExpressionNode[realArgs.length];
            InlineType type = setupInline(newArgs);
            if (type == InlineType.FULL) {
                setupEvaluate(newArgs);
                return NodeConstantBoolean.of(node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncBoolean(newArgs);
            }
            return this;
        }
    }
}
