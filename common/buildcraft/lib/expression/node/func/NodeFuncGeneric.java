package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.*;

public abstract class NodeFuncGeneric implements INodeFunc {

    private final IExpressionNode node;
    protected final NodeType[] types;
    protected final IVariableNode[] variables;

    public NodeFuncGeneric(IExpressionNode node, NodeType[] types, IVariableNode[] nodes) {
        this.node = node;
        this.types = types;
        this.variables = nodes;

        if (types.length != nodes.length) {
            throw new IllegalArgumentException("Lengths did not match! (" + types.length + " vs " + nodes.length + ")");
        }
        for (int i = 0; i < types.length; i++) {
            NodeType givenType = types[i];
            if (NodeType.getType(nodes[i]) != givenType) {
                throw new IllegalArgumentException("Types did not match! (given " + givenType + ", node is " + nodes[i].getClass() + ")");
            }
        }
    }

    protected IExpressionNode[] popArgs(INodeStack stack) throws InvalidExpressionException {
        IExpressionNode[] nodes = new IExpressionNode[types.length];
        for (int i = types.length; i > 0; i--) {
            nodes[i-1] = stack.pop(types[i-1]);
        }
        return nodes;
    }

    @Override
    public String toString() {
        return "somefunc(" + node.toString() + ")";
    }

    protected abstract class Func implements IExpressionNode {
        protected final IExpressionNode[] realArgs;

        public Func(IExpressionNode[] argsIn) {
            this.realArgs = argsIn;
        }

        protected void setupEvaluate(IExpressionNode[] nodes) {
            for (int i = 0; i < nodes.length; i++) {
                variables[i].set(nodes[i]);
            }
        }

        protected InlineType setupInline(IExpressionNode[] nodes) {
            InlineType type = InlineType.FULL;
            for (int i = 0; i < realArgs.length; i++) {
                IExpressionNode bef = realArgs[i];
                IExpressionNode aft = bef.inline();
                nodes[i] = aft;
                type = type.and(bef, aft);
            }
            return type;
        }

        protected String getArgsToString() {
            String total = "[";

            for (int i = 0; i < realArgs.length; i++) {
                if (i > 0) {
                    total += ", (";
                } else {
                    total += " (";
                }

                total += realArgs[i].toString() + ") ";
            }

            return total + "]";
        }

        @Override
        public String toString() {
            return "[" + getArgsToString() + " -> generic]";
        }
    }

    public enum InlineType {
        NONE,
        PARTIAL,
        FULL;

        public InlineType and(IExpressionNode before, IExpressionNode after) {
            if (this == PARTIAL) return PARTIAL;
            else if (this == NONE) {
                return before == after ? NONE : PARTIAL;
            } else {// FULL
                return after instanceof IConstantNode ? FULL : PARTIAL;
            }
        }
    }
}
