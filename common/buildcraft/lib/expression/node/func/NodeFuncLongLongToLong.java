package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncLongLongToLong implements INodeFuncLong {

    public final IFuncLongLongToLong function;
    private final StringFunctionTri stringFunction;

    public NodeFuncLongLongToLong(IFuncLongLongToLong function) {
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncLongLongToLong(IFuncLongLongToLong function, String fnString) {
        this(function, (a, b) -> "[" + a + ", " + b + "] " + fnString);
    }

    public NodeFuncLongLongToLong(IFuncLongLongToLong function, StringFunctionTri stringFunction) {
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[long, long -> long] {" + function.toString() + "}" : stringFunction.apply("{0}", "{1}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        INodeLong b = stack.popLong();
        INodeLong a = stack.popLong();
        return new Func(a, b, function, stringFunction);
    }

    private static class Func implements INodeLong {
        private final INodeLong a, b;
        private final IFuncLongLongToLong function;
        private final StringFunctionTri stringFunction;

        public Func(INodeLong a, INodeLong b, IFuncLongLongToLong function, StringFunctionTri stringFunction) {
            this.a = a;
            this.b = b;
            this.function = function;
            this.stringFunction = stringFunction;
        }

        @Override
        public long evaluate() {
            return function.apply(a.evaluate(), b.evaluate());
        }

        @Override
        public INodeLong inline() {
            return NodeInliningHelper.tryInline(this, a, b, (a, b) -> new Func(a, b, function, stringFunction),//
                (a, b) -> new NodeConstantLong(function.apply(a.evaluate(), b.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + a + ", " + b + " -> long] {" + function.toString() + "}"//
                : stringFunction.apply(a.toString(), b.toString());
        }
    }

    public interface IFuncLongLongToLong {
        long apply(long a, long b);
    }
}
