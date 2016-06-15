package buildcraft.lib.expression;

import org.apache.commons.lang3.tuple.Pair;

import buildcraft.lib.expression.api.ArgumentCounts;
import buildcraft.lib.expression.api.IExpression;
import buildcraft.lib.expression.api.IExpression.IExpressionBoolean;
import buildcraft.lib.expression.api.IExpression.IExpressionDouble;
import buildcraft.lib.expression.api.IExpression.IExpressionLong;
import buildcraft.lib.expression.api.IExpression.IExpressionString;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.cast.NodeCasting;
import buildcraft.lib.expression.node.func.ExpressionBoolean;
import buildcraft.lib.expression.node.func.ExpressionDouble;
import buildcraft.lib.expression.node.func.ExpressionLong;
import buildcraft.lib.expression.node.func.ExpressionString;
import buildcraft.lib.expression.api.IFunctionMap;

public class GenericExpressionCompiler {

    public static IExpressionLong compileExpressionLong(String function) throws InvalidExpressionException {
        return compileExpressionLong(function, null);
    }

    public static IExpressionLong compileExpressionLong(String function, IFunctionMap functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);

        if (exp.getLeft() instanceof INodeLong) {
            return new ExpressionLong((INodeLong) exp.getLeft(), exp.getRight());
        }

        throw new InvalidExpressionException("Not a long " + exp.getLeft());
    }

    public static IExpressionDouble compileExpressionDouble(String function) throws InvalidExpressionException {
        return compileExpressionDouble(function, null);
    }

    public static IExpressionDouble compileExpressionDouble(String function, IFunctionMap functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        INodeDouble nodeDouble = NodeCasting.castToDouble(exp.getLeft());
        return new ExpressionDouble(nodeDouble, exp.getRight());
    }

    public static IExpressionBoolean compileExpressionBoolean(String function) throws InvalidExpressionException {
        return compileExpressionBoolean(function, null);
    }

    public static IExpressionBoolean compileExpressionBoolean(String function, IFunctionMap functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);

        if (exp.getLeft() instanceof INodeBoolean) {
            return new ExpressionBoolean((INodeBoolean) exp.getLeft(), exp.getRight());
        }

        throw new InvalidExpressionException("Not a boolean " + exp.getLeft());
    }

    public static IExpressionString compileExpressionString(String function) throws InvalidExpressionException {
        return compileExpressionString(function, null);
    }

    public static IExpressionString compileExpressionString(String function, IFunctionMap functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        INodeString nodeString = NodeCasting.castToString(exp.getLeft());
        return new ExpressionString(nodeString, exp.getRight());
    }

    public static IExpression compileExpressionUnknown(String function) throws InvalidExpressionException {
        return compileExpressionUnknown(function, null);
    }

    public static IExpression compileExpressionUnknown(String function, IFunctionMap functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        IExpressionNode node = exp.getLeft();
        if (node instanceof INodeString) {
            return new ExpressionString((INodeString) node, exp.getRight());
        }
        if (node instanceof INodeBoolean) {
            return new ExpressionBoolean((INodeBoolean) node, exp.getRight());
        }
        if (node instanceof INodeLong) {
            return new ExpressionLong((INodeLong) node, exp.getRight());
        }
        if (node instanceof INodeDouble) {
            return new ExpressionDouble((INodeDouble) node, exp.getRight());
        }
        throw new InvalidExpressionException("Unknown node type " + node);
    }

    private static Pair<IExpressionNode, ArgumentCounts> compileExpression(String function, IFunctionMap functions) throws InvalidExpressionException {
        return InternalCompiler.compileExpression(function, functions);
    }
}
