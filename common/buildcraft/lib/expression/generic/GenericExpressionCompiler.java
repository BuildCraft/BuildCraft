package buildcraft.lib.expression.generic;

import java.util.Map;

import buildcraft.lib.expression.generic.IExpressionNode.INodeLong;

public class GenericExpressionCompiler {

    public static ExpressionLong compileLong(String expression, Map<String, IExpression> functions) throws InvalidExpressionException {
        IExpressionNode node = compileExpression(expression, functions);
        if (node instanceof INodeLong) {
            return new ExpressionLong(node, counts);
        }
    }

    private static IExpressionNode compileExpression(String expression, Map<String, IExpression> functions) throws InvalidExpressionException {
        expression = InternalCompiler.validateExpression(expression);
        String[] split = InternalCompiler.split(expression);
        String[] postfix = InternalCompiler.convertToPostfix(split);
        return InternalCompiler.makeExpression(postfix, functions);
    }
}
