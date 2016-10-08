package buildcraft.lib.expression;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
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

public class GenericExpressionCompiler {
    public static final boolean DEBUG = BCDebugging.shouldDebugComplex("lib.expression");
    /** Modifiable field to enable or disable debugging for testing. You should reset this to {@link #DEBUG} after you
     * have finished testing. */
    public static boolean debug = DEBUG;
    private static String debugIndentCache = "";

    public static IExpressionLong compileExpressionLong(String function) throws InvalidExpressionException {
        return compileExpressionLong(function, null);
    }

    public static IExpressionLong compileExpressionLong(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);

        if (exp.getLeft() instanceof INodeLong) {
            return new ExpressionLong((INodeLong) exp.getLeft(), exp.getRight());
        }

        throw new InvalidExpressionException("Not a long " + exp.getLeft());
    }

    public static IExpressionDouble compileExpressionDouble(String function) throws InvalidExpressionException {
        return compileExpressionDouble(function, null);
    }

    public static IExpressionDouble compileExpressionDouble(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        INodeDouble nodeDouble = NodeCasting.castToDouble(exp.getLeft());
        return new ExpressionDouble(nodeDouble, exp.getRight());
    }

    public static IExpressionBoolean compileExpressionBoolean(String function) throws InvalidExpressionException {
        return compileExpressionBoolean(function, null);
    }

    public static IExpressionBoolean compileExpressionBoolean(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);

        if (exp.getLeft() instanceof INodeBoolean) {
            return new ExpressionBoolean((INodeBoolean) exp.getLeft(), exp.getRight());
        }

        throw new InvalidExpressionException("Not a boolean " + exp.getLeft());
    }

    public static IExpressionString compileExpressionString(String function) throws InvalidExpressionException {
        return compileExpressionString(function, null);
    }

    public static IExpressionString compileExpressionString(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        INodeString nodeString = NodeCasting.castToString(exp.getLeft());
        return new ExpressionString(nodeString, exp.getRight());
    }

    public static IExpression compileExpressionUnknown(String function) throws InvalidExpressionException {
        return compileExpressionUnknown(function, null);
    }

    public static IExpression compileExpressionUnknown(String function, FunctionContext functions) throws InvalidExpressionException {
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

    private static Pair<IExpressionNode, ArgumentCounts> compileExpression(String function, FunctionContext context) throws InvalidExpressionException {
        return InternalCompiler.compileExpression(function, context);
    }

    public static void debugStart(String text) {
        if (debug) {
            debugPrintln(text);
            debugIndentCache += "  ";
        }
    }

    public static void debugEnd(String text) {
        if (debug) {
            if (debugIndentCache.length() > 1) {
                debugIndentCache = debugIndentCache.substring(2);
            } else if (debugIndentCache.length() > 0) {
                debugIndentCache = "";
            }
            debugPrintln(text);
        }
    }

    public static void debugPrintln(String text) {
        if (debug) {
            if (Loader.instance().hasReachedState(LoaderState.CONSTRUCTING)) {
                BCLog.logger.info(debugIndentCache + text);
            } else {
                // When using a test
                System.out.println(debugIndentCache + text);
            }
        }
    }
}
