package buildcraft.lib.expression.node.simple;

import java.util.function.LongUnaryOperator;

import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class NodeUnaryLong implements INodeLong {
    public enum Type {
        NEG((v) -> -v),
        BITWISE_INVERT(v -> ~v);

        private final LongUnaryOperator operator;

        Type(LongUnaryOperator operator) {
            this.operator = operator;
        }

        public NodeUnaryLong create(INodeLong from) {
            return new NodeUnaryLong(from, this);
        }
    }

    private final INodeLong from;
    private final Type type;

    private NodeUnaryLong(INodeLong from, Type type) {
        this.from = from;
        this.type = type;
    }

    @Override
    public long evaluate() {
        return type.operator.applyAsLong(from.evaluate());
    }

    @Override
    public INodeLong inline(Arguments args) {
        INodeLong from = this.from.inline(args);
        if (from instanceof NodeValueLong) {
            return new NodeValueLong(type.operator.applyAsLong(((NodeValueLong) from).value));
        } else if (from == this.from) {
            return this;
        } else {
            return new NodeUnaryLong(from, type);
        }
    }

    @Override
    public String toString() {
        return type.name() + "(" + from + ")";
    }
}
