package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeFuncObjectLongToObject<F, T> implements INodeFuncObject<T> {

    public final Class<F> argType;
    public final Class<T> returnType;
    public final IFuncObjectLongToObject<F, T> function;
    private final StringFunctionTri stringFunction;

    public NodeFuncObjectLongToObject(Class<F> argType, Class<T> returnType, IFuncObjectLongToObject<F, T> function) {
        this.argType = argType;
        this.returnType = returnType;
        this.function = function;
        this.stringFunction = null;
    }

    public NodeFuncObjectLongToObject(Class<F> argType, Class<T> returnType, IFuncObjectLongToObject<F, T> function,
        String fnString) {
        this(argType, returnType, function, (a, b) -> "[" + a + ", " + b + "] " + fnString);
    }

    public NodeFuncObjectLongToObject(Class<F> argType, Class<T> returnType, IFuncObjectLongToObject<F, T> function,
        StringFunctionTri stringFunction) {
        this.argType = argType;
        this.returnType = returnType;
        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public Class<T> getType() {
        return returnType;
    }

    @Override
    public String toString() {
        return stringFunction == null ? "[long, long -> long] {" + function.toString() + "}"
            : stringFunction.apply("{0}", "{1}");
    }

    @Override
    public INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
        INodeLong b = stack.popLong();
        INodeObject<F> a = stack.popObject(argType);
        return new Func(a, b);
    }

    private class Func implements INodeObject<T> {
        private final INodeObject<F> a;
        private final INodeLong b;

        public Func(INodeObject<F> a, INodeLong b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public Class<T> getType() {
            return returnType;
        }

        @Override
        public T evaluate() {
            return function.apply(a.evaluate(), b.evaluate());
        }

        @Override
        public INodeObject<T> inline() {
            return NodeInliningHelper.tryInline(this, a, b, (a, b) -> new Func(a, b),//
                (a, b) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate(), b.evaluate())));
        }

        @Override
        public String toString() {
            return stringFunction == null//
                ? "[" + a + ", " + b + " -> long] {" + function.toString() + "}"//
                : stringFunction.apply(a.toString(), b.toString());
        }
    }

    public interface IFuncObjectLongToObject<F, T> {
        T apply(F a, long b);
    }
}
