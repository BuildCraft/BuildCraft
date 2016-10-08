package buildcraft.lib.expression.node.unary;

import java.util.function.LongUnaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.Arguments;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeImmutableLong;

public class NodeUnaryLong implements INodeLong {
    public enum Type {
        NEG("-", (v) -> -v),
        BITWISE_INVERT("~", v -> ~v);

        private final String op;
        private final LongUnaryOperator operator;

        Type(String op, LongUnaryOperator operator) {
            this.op = op;
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
        return NodeInliningHelper.tryInline(this, args, from, //
                (f) -> new NodeUnaryLong(f, type), //
                (f) -> new NodeImmutableLong(type.operator.applyAsLong(f.evaluate())));
    }

    @Override
    public String toString() {
        return type.op + "(" + from + ")";
    }
}
