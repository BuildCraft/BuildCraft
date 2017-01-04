package buildcraft.lib.expression.node.unary;

import java.util.function.DoubleUnaryOperator;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeUnaryDouble implements INodeDouble {
    public enum Type {
        NEG("-", (v) -> -v);

        private final String op;
        private final DoubleUnaryOperator operator;

        Type(String op, DoubleUnaryOperator operator) {
            this.op = op;
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
    public INodeDouble inline() {
        return NodeInliningHelper.tryInline(this, from, (f) -> new NodeUnaryDouble(f, type), //
                (f) -> new NodeConstantDouble(type.operator.applyAsDouble(f.evaluate())));
    }

    @Override
    public String toString() {
        return type.op + "(" + from + ")";
    }
}
