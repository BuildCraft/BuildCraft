/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.cast.NodeCasting;

public class GenericExpressionCompiler {

    // Long support

    public static INodeLong compileExpressionLong(String function) throws InvalidExpressionException {
        return compileExpressionLong(function, DefaultContexts.createWithAll());
    }

    public static INodeLong compileExpressionLong(String function, FunctionContext context) throws InvalidExpressionException {
        IExpressionNode node = InternalCompiler.compileExpression(function, context);
        if (node instanceof INodeLong) {
            return (INodeLong) node;
        } else {
            throw new InvalidExpressionException("Not a long " + node);
        }
    }

    public static INodeFuncLong compileFunctionLong(String function, Argument... args) throws InvalidExpressionException {
        return compileFunctionLong(function, DefaultContexts.createWithAll(), args);
    }

    public static INodeFuncLong compileFunctionLong(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
        INodeFunc func = InternalCompiler.compileFunction(function, context, args);

        if (func instanceof INodeFuncLong) {
            return (INodeFuncLong) func;
        } else {
            throw new InvalidExpressionException("Not a long " + func);
        }
    }

    // Double support

    public static INodeDouble compileExpressionDouble(String function) throws InvalidExpressionException {
        return compileExpressionDouble(function, DefaultContexts.createWithAll());
    }

    public static INodeDouble compileExpressionDouble(String function, FunctionContext context) throws InvalidExpressionException {
        return NodeCasting.castToDouble(InternalCompiler.compileExpression(function, context)).inline();
    }

    public static INodeFuncDouble compileFunctionDouble(String function, Argument... args) throws InvalidExpressionException {
        return compileFunctionDouble(function, DefaultContexts.createWithAll(), args);
    }

    public static INodeFuncDouble compileFunctionDouble(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
        return NodeCasting.castToDouble(InternalCompiler.compileFunction(function, context, args));
    }

    // Boolean support

    public static INodeBoolean compileExpressionBoolean(String function) throws InvalidExpressionException {
        return compileExpressionBoolean(function, DefaultContexts.createWithAll());
    }

    public static INodeBoolean compileExpressionBoolean(String function, FunctionContext context) throws InvalidExpressionException {
        IExpressionNode node = InternalCompiler.compileExpression(function, context);
        if (node instanceof INodeBoolean) {
            return (INodeBoolean) node;
        } else {
            throw new InvalidExpressionException("Not a boolean " + node);
        }
    }

    public static INodeFuncBoolean compileFunctionBoolean(String function, Argument... args) throws InvalidExpressionException {
        return compileFunctionBoolean(function, DefaultContexts.createWithAll(), args);
    }

    public static INodeFuncBoolean compileFunctionBoolean(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
        INodeFunc func = InternalCompiler.compileFunction(function, context, args);

        if (func instanceof INodeFuncBoolean) {
            return (INodeFuncBoolean) func;
        } else {
            throw new InvalidExpressionException("Not a boolean " + func);
        }
    }

    // Object support

    public static <T> INodeObject<T> compileExpressionObject(Class<T> clazz, String function) throws InvalidExpressionException {
        return compileExpressionObject(clazz, function, DefaultContexts.createWithAll());
    }

    public static <T> INodeObject<T> compileExpressionObject(Class<T> clazz, String function, FunctionContext context) throws InvalidExpressionException {
        return NodeCasting.castToObject(InternalCompiler.compileExpression(function, context), clazz);
    }

    public static <T> INodeFuncObject<T> compileFunctionObject(Class<T> clazz, String function, Argument... args) throws InvalidExpressionException {
        return compileFunctionObject(clazz, function, DefaultContexts.createWithAll(), args);
    }

    public static <T> INodeFuncObject<T> compileFunctionObject(Class<T> clazz, String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
        return NodeCasting.castToObject(InternalCompiler.compileFunction(function, context, args), clazz);
    }

    // String support

    public static INodeObject<String> compileExpressionString(String function) throws InvalidExpressionException {
        return compileExpressionString(function, DefaultContexts.createWithAll());
    }

    public static INodeObject<String> compileExpressionString(String function, FunctionContext context) throws InvalidExpressionException {
        return NodeCasting.castToString(InternalCompiler.compileExpression(function, context)).inline();
    }

    public static INodeFuncObject<String> compileFunctionString(String function, Argument... args) throws InvalidExpressionException {
        return compileFunctionString(function, DefaultContexts.createWithAll(), args);
    }

    public static INodeFuncObject<String> compileFunctionString(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
        return NodeCasting.castToString(InternalCompiler.compileFunction(function, context, args));
    }
}
