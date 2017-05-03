package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.*;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeFuncGenericToDouble extends NodeFuncGeneric implements INodeFuncDouble {

    protected final INodeDouble node;

    public NodeFuncGenericToDouble(INodeDouble node, NodeType[] types, IVariableNode[] nodes) {
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
