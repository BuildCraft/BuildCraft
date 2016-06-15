package buildcraft.lib.expression.api;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpression.IExpressionLong;
import buildcraft.lib.expression.api.IExpression.IExpressionString;

public interface IFunctionMap {
    IInnerMap<IExpressionLong> getLongMap();

    IInnerMap<IExpressionDouble> getDoubleMap();

    IInnerMap<IExpressionBoolean> getBooleanMap();

    IInnerMap<IExpressionString> getStringMap();

    default Collection<IExpression> getExpressions(String name) {
        ImmutableList.Builder<IExpression> builder = ImmutableList.builder();
        builder.addAll(getLongMap().getExpressions(name));
        builder.addAll(getDoubleMap().getExpressions(name));
        builder.addAll(getBooleanMap().getExpressions(name));
        builder.addAll(getStringMap().getExpressions(name));
        return builder.build();
    }

    default Collection<IExpression> getExpressions(String name, int numArgs) {
        ImmutableList.Builder<IExpression> builder = ImmutableList.builder();
        builder.addAll(getLongMap().getExpressions(name, numArgs));
        builder.addAll(getDoubleMap().getExpressions(name, numArgs));
        builder.addAll(getBooleanMap().getExpressions(name, numArgs));
        builder.addAll(getStringMap().getExpressions(name, numArgs));
        return builder.build();
    }

    default IExpression getExpression(FunctionIdentifier identifer) {
        IExpression exp = getLongMap().getExpression(identifer);
        if (exp != null) {
            return exp;
        }
        exp = getDoubleMap().getExpression(identifer);
        if (exp != null) {
            return exp;
        }
        exp = getBooleanMap().getExpression(identifer);
        if (exp != null) {
            return exp;
        }
        return getStringMap().getExpression(identifer);
    }

    static void append(Builder<IExpression> builder, IExpression expression) {
        if (expression != null) {
            builder.add(expression);
        }
    }

    public interface IInnerMap<E extends IExpression> {
        Collection<E> getExpressions(String name);

        Collection<E> getExpressions(String name, int numArgs);

        E getExpression(FunctionIdentifier identifer);

        FunctionIdentifier putExpression(String name, E expression);
    }
}
