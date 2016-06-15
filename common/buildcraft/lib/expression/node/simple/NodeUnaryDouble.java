package buildcraft.lib.expression.node.simple;

import java.util.function.DoubleUnaryOperator;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class NodeUnaryDouble implements INodeDouble {
    public enum Type {
        NEG((v) -> -v);

        private final DoubleUnaryOperator operator;

        Type(DoubleUnaryOperator operator) {
            this.operator = operator;
        }

        public NodeUnaryDouble create(INodeDouble from) {
            return new NodeUnaryDouble(from, this);
        }
    }

    private final INodeDouble from;
    private final Type type;

    private NodeUnaryDouble(INodeDouble from, Type type) {
        this.from = from;
        this.type = type;
    }

    @Override
    public double evaluate() {
        return type.operator.applyAsDouble(from.evaluate());
    }

    @Override
    public INodeDouble inline(Arguments args) {
        INodeDouble from = this.from.inline(args);
        if (from instanceof NodeValueDouble) {
            return new NodeValueDouble(type.operator.applyAsDouble(((NodeValueDouble) from).value));
        } else if (from == this.from) {
            return this;
        } else {
            return new NodeUnaryDouble(from, type);
        }
    }

    @Override
    public String toString() {
        return type.name() + "(" + from + ")";
    }
}
