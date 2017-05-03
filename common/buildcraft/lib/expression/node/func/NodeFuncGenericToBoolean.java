package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.*;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeFuncGenericToBoolean extends NodeFuncGeneric implements INodeFuncBoolean {

    protected final INodeBoolean node;

    public NodeFuncGenericToBoolean(INodeBoolean node, NodeType[] types, IVariableNode[] nodes) {
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
                return NodeConstantBoolean.get(node.evaluate());
            } else if (type == InlineType.PARTIAL) {
                return new FuncBoolean(newArgs);
            }
            return this;
        }
    }
}
