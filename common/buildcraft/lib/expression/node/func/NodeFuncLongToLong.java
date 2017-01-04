package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncLongToLong implements INodeFuncLong {

    public final IFuncLongToLong function;
    private final StringFunctionBi stringFunction;

    public NodeFuncLongToLong(IFuncLongToLong function) {
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncLongToLong(IFuncLongToLong function, String fnString) {
        this(new IFuncLongToLong() {
            @Override
            public long apply(long arg) {
                return function.apply(arg);
            }

            @Override
            public String toString() {
                return fnString;
            }
        });
    }

    public NodeFuncLongToLong(IFuncLongToLong function, StringFunctionBi stringFunction) {
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[long -> long] {" + function.toString() + "}" : stringFunction.apply("{0}");
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
        return new Func(stack.popLong(), function, stringFunction);
    }

    private static class Func implements INodeLong {
        private final INodeLong arg;
        private final IFuncLongToLong function;
        private final StringFunctionBi stringFunction;

        public Func(INodeLong arg, IFuncLongToLong function, StringFunctionBi stringFunction) {
            this.arg = arg;
            this.function = function;
            this.stringFunction = stringFunction;
        }

        @Override
        public long evaluate() {
            return function.apply(arg.evaluate());
        }

        @Override
        public INodeLong inline() {
            return NodeInliningHelper.tryInline(this, arg, (a) -> new Func(a, function, stringFunction),//
                    (a) -> new NodeConstantLong(function.apply(a.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + arg + " -> long] {" + function.toString() + "}"//
                : stringFunction.apply(function.toString());
        }
    }

    public interface IFuncLongToLong {
        long apply(long arg);
    }
}
