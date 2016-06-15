package buildcraft.lib.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import buildcraft.lib.expression.api.FunctionIdentifier;
import buildcraft.lib.expression.api.IExpression;
import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpression.IExpressionLong;
import buildcraft.lib.expression.api.IExpression.IExpressionString;
import buildcraft.lib.expression.api.IFunctionMap;

public class FunctionMap implements IFunctionMap {
    private final InnerMap<IExpressionLong> longFunctions = new InnerMap<>();
    private final InnerMap<IExpressionDouble> doubleFunctions = new InnerMap<>();
    private final InnerMap<IExpressionBoolean> booleanFunctions = new InnerMap<>();
    private final InnerMap<IExpressionString> stringFunctions = new InnerMap<>();

    @Override
    public IInnerMap<IExpressionLong> getLongMap() {
        return longFunctions;
    }

    @Override
    public IInnerMap<IExpressionDouble> getDoubleMap() {
        return doubleFunctions;
    }

    @Override
    public IInnerMap<IExpressionBoolean> getBooleanMap() {
        return booleanFunctions;
    }

    @Override
    public IInnerMap<IExpressionString> getStringMap() {
        return stringFunctions;
    }

    public class InnerMap<E extends IExpression> implements IInnerMap<E> {
        private final Map<FunctionIdentifier, E> functions = new HashMap<>();
        private final Multimap<String, E> functionNames = HashMultimap.create();

        @Override
        public FunctionIdentifier putExpression(String name, E expression) {
            FunctionIdentifier ident = new FunctionIdentifier(name, expression.getCounts());

            IExpression existing = FunctionMap.this.getExpression(ident);
            if (existing != null) {
                throw new IllegalStateException("You cannot add multiple functions with differing return types!");
            }

            E old = functions.put(ident, expression);
            if (old != null) {
                functionNames.remove(ident.lowerCaseName, old);
            }
            functionNames.put(ident.lowerCaseName, expression);

            System.out.println("Defined a function " + ident);

            return ident;
        }

        @Override
        public Collection<E> getExpressions(String name) {
            return ImmutableList.copyOf(functionNames.get(name));
        }

        @Override
        public Collection<E> getExpressions(String name, int numArgs) {
            ImmutableList.Builder<E> matching = ImmutableList.builder();
            for (E element : functionNames.get(name)) {
                if (element.getCounts().order.size() == numArgs) {
                    matching.add(element);
                }
            }
            return matching.build();
        }

        @Override
        public E getExpression(FunctionIdentifier identifer) {
            return functions.get(identifer);
        }
    }
}
