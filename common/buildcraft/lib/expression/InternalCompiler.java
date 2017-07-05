/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.binary.BiNodeToBooleanType;
import buildcraft.lib.expression.node.binary.BiNodeType;
import buildcraft.lib.expression.node.binary.IBinaryNodeType;
import buildcraft.lib.expression.node.cast.NodeCastBooleanToString;
import buildcraft.lib.expression.node.cast.NodeCastDoubleToString;
import buildcraft.lib.expression.node.cast.NodeCastLongToDouble;
import buildcraft.lib.expression.node.cast.NodeCastLongToString;
import buildcraft.lib.expression.node.condition.NodeConditionalBoolean;
import buildcraft.lib.expression.node.condition.NodeConditionalDouble;
import buildcraft.lib.expression.node.condition.NodeConditionalLong;
import buildcraft.lib.expression.node.condition.NodeConditionalString;
import buildcraft.lib.expression.node.func.NodeFuncGenericToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncGenericToDouble;
import buildcraft.lib.expression.node.func.NodeFuncGenericToLong;
import buildcraft.lib.expression.node.func.NodeFuncGenericToString;
import buildcraft.lib.expression.node.unary.IUnaryNodeType;
import buildcraft.lib.expression.node.unary.UnaryNodeType;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantString;

public class InternalCompiler {
    private static final String UNARY_NEGATION = "¬";
    private static final String FUNCTION_START = "@";
    private static final String FUNCTION_ARGS = "#";
    private static final String OPERATORS = "+-*/^%~?:& << >> >>> == <= >= && || !=";
    private static final String leftAssociative = "+-^*/%||&&==!=<=>=<<>>?&";
    private static final String rightAssociative = "";
    private static final String[] precedence = { "(),", "?", "&& ||", "!= == <= >=", "<< >>", "+-", "%", "*/", "^", "~¬" };

    private static final String LONG_REGEX = "[-+]?(0x[0-9a-fA-F_]+|[0-9]+)";
    private static final String DOUBLE_REGEX = "[-+]?[0-9]+(\\.[0-9]+)?";
    private static final String BOOLEAN_REGEX = "true|false";
    private static final String STRING_REGEX = "'.*'";

    private static final Pattern LONG_MATCHER = Pattern.compile(LONG_REGEX);
    private static final Pattern DOUBLE_MATCHER = Pattern.compile(DOUBLE_REGEX);
    private static final Pattern BOOLEAN_MATCHER = Pattern.compile(BOOLEAN_REGEX);
    private static final Pattern STRING_MATCHER = Pattern.compile(STRING_REGEX);

    public static IExpressionNode compileExpression(String expression, FunctionContext context) throws InvalidExpressionException {
        try {
            ExpressionDebugManager.debugPrintln("Compiling " + expression);
            String[] infix = tokenize(expression);
            String[] postfix = convertToPostfix(infix);
            ExpressionDebugManager.debugPrintln(Arrays.toString(postfix));
            return makeExpression(postfix, context);
        } catch (InvalidExpressionException iee) {
            throw new InvalidExpressionException("Failed to compile expression " + expression, iee);
        }
    }

    public static INodeFunc compileFunction(String expression, FunctionContext context, Argument... args) throws InvalidExpressionException {
        FunctionContext ctxReal = new FunctionContext(context);

        IVariableNode[] nodes = new IVariableNode[args.length];
        NodeType[] types = new NodeType[args.length];
        for (int i = 0; i < nodes.length; i++) {
            types[i] = args[i].type;
            nodes[i] = ctxReal.putVariable(args[i].name, args[i].type);
        }

        IExpressionNode node = compileExpression(expression, ctxReal);
        if (node instanceof INodeLong) {
            return new NodeFuncGenericToLong((INodeLong) node, types, nodes);
        } else if (node instanceof INodeDouble) {
            return new NodeFuncGenericToDouble((INodeDouble) node, types, nodes);
        } else if (node instanceof INodeBoolean) {
            return new NodeFuncGenericToBoolean((INodeBoolean) node, types, nodes);
        } else if (node instanceof INodeString) {
            return new NodeFuncGenericToString((INodeString) node, types, nodes);
        } else {
            ExpressionDebugManager.debugNodeClass(node.getClass());
            throw new IllegalStateException("Unknown node " + node.getClass());
        }
    }

    private static String[] tokenize(String function) throws InvalidExpressionException {
        return TokenizerDefaults.createTokenizer().tokenize(function);
    }

    private static int getPrecedence(String token) {
        int p = 0;
        if (token.startsWith(FUNCTION_START)) {
            return 0;
        }
        for (String pre : precedence) {
            if (pre.contains(token)) {
                return p;
            }
            p++;
        }
        return p;
    }

    private static String[] convertToPostfix(String[] infix) throws InvalidExpressionException {
        // Implementation of https://en.wikipedia.org/wiki/Shunting-yard_algorithm
        Deque<String> stack = new ArrayDeque<>();
        List<String> postfix = new ArrayList<>();
        int index = 0;
        ExpressionDebugManager.debugPrintln("Converting " + Arrays.toString(infix));
        ExpressionDebugManager.debugPrintln("         Stack=" + stack + ", postfix=" + postfix);
        boolean justPushedFunc = false;
        for (index = 0; index < infix.length; index++) {
            String token = infix[index];
            ExpressionDebugManager.debugPrintln("  - Token \"" + token + "\"");

            if (justPushedFunc && !")".equals(token)) {
                if (stack.peek() != null && stack.peek().startsWith(FUNCTION_START)) {
                    stack.push(",");
                }
            }
            justPushedFunc = false;

            if (",".equals(token)) {
                int commas = 1;
                boolean found = false;
                while (!stack.isEmpty()) {
                    String fromStack = stack.pop();
                    if ("(".equals(fromStack) || fromStack.startsWith(FUNCTION_START)) {
                        found = true;
                        stack.push(fromStack);
                        break;
                    } else if (",".equals(fromStack)) {
                        commas++;
                    } else {
                        postfix.add(fromStack);
                    }
                }
                for (int i = 0; i < commas; i++) {
                    stack.push(",");
                }
                if (!found) {
                    throw new InvalidExpressionException("Did not find an opening parenthesis for the comma!");
                }
            } else if ("(".equals(token)) {
                stack.push(token);
            } else if (")".equals(token)) {
                int commas = 0;
                boolean found = false;
                while (!stack.isEmpty()) {
                    String fromStack = stack.pop();
                    if ("(".equals(fromStack)) {
                        found = true;
                        break;
                    } else if (fromStack.startsWith(FUNCTION_START)) {
                        found = true;
                        // Add it back onto the stack to be used later
                        fromStack = fromStack + FUNCTION_ARGS + commas;
                        postfix.add(fromStack);
                        break;
                    } else if (",".equals(fromStack)) {
                        commas++;
                    } else {
                        postfix.add(fromStack);
                    }
                }
                if (!found) {
                    throw new InvalidExpressionException("Too many closing parenthesis!");
                }
            } else if (":".equals(token)) {
                String s;
                while ((s = stack.peek()) != null) {
                    if ("?".equals(s)) {
                        break;
                    } else {
                        postfix.add(stack.pop());
                    }
                }
            } else if (OPERATORS.contains(token)) {
                // Its an operator
                if ("-".equals(token) && (index == 0 || (OPERATORS + "(,").contains(infix[index - 1]))) {
                    token = UNARY_NEGATION;
                }

                String s;
                while ((s = stack.peek()) != null) {
                    int tokenPrec = getPrecedence(token);
                    int stackPrec = getPrecedence(s);
                    boolean continueIfEqual = !"?".contains(token);

                    boolean shouldContinue = leftAssociative.contains(token) && (continueIfEqual ? tokenPrec <= stackPrec : tokenPrec < stackPrec);
                    if (!shouldContinue && rightAssociative.contains(token)) {
                        if (tokenPrec > stackPrec) shouldContinue = true;
                    }

                    if (shouldContinue) {
                        postfix.add(stack.pop());
                    } else {
                        break;
                    }
                }
                stack.push(token);
            } else if (index + 1 < infix.length && "(".equals(infix[index + 1])) {
                justPushedFunc = true;
                // Its a function (The next token is an open parenthesis)
                stack.push(FUNCTION_START + token);
                // Also ignore the parenthesis (the function is treated as if it was an open parenthesis)
                index++;
            } else {

                // Either an argument, number, string or boolean
                postfix.add(token);
            }
            ExpressionDebugManager.debugPrintln("         Stack=" + stack + ", postfix=" + postfix);
        }

        while (!stack.isEmpty()) {
            String operator = stack.pop();
            ExpressionDebugManager.debugPrintln("  - Operator \"" + operator + "\"");
            if ("(".equals(operator)) {
                throw new InvalidExpressionException("Too many opening parenthesis!");
            } else if (")".equals(operator)) {
                throw new InvalidExpressionException("Too many closing parenthesis!");
            } else {
                postfix.add(operator);
            }
            ExpressionDebugManager.debugPrintln("         Stack=" + stack + ", postfix=" + postfix);
        }

        return postfix.toArray(new String[postfix.size()]);
    }

    private static IExpressionNode makeExpression(String[] postfix, FunctionContext context) throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        for (String op : postfix) {
            if ("-".equals(op)) pushBiNode(stack, BiNodeType.SUB);
            else if ("+".equals(op)) pushBiNode(stack, BiNodeType.ADD);
            else if ("^".equals(op)) pushBiNode(stack, BiNodeType.XOR);
            else if ("&".equals(op)) pushBiNode(stack, BiNodeType.AND);
            else if ("|".equals(op)) pushBiNode(stack, BiNodeType.OR);
            else if ("&&".equals(op)) pushBiNode(stack, BiNodeType.AND);
            else if ("||".equals(op)) pushBiNode(stack, BiNodeType.OR);
            else if ("*".equals(op)) pushBiNode(stack, BiNodeType.MUL);
            else if ("/".equals(op)) pushBiNode(stack, BiNodeType.DIV);
            else if ("%".equals(op)) pushBiNode(stack, BiNodeType.MOD);
            else if (">>".equals(op)) pushBiNode(stack, BiNodeType.SHIFT_LEFT);
            else if ("<<".equals(op)) pushBiNode(stack, BiNodeType.SHIFT_RIGHT);
            else if ("~".equals(op)) pushUnaryNode(stack, UnaryNodeType.BITWISE_INVERT);
            else if ("==".equals(op)) pushBiNode(stack, BiNodeToBooleanType.EQUAL);
            else if ("!=".equals(op)) pushBiNode(stack, BiNodeToBooleanType.NOT_EQUAL);
            else if ("<=".equals(op)) pushBiNode(stack, BiNodeToBooleanType.LESS_THAN_OR_EQUAL);
            else if (">=".equals(op)) pushBiNode(stack, BiNodeToBooleanType.GREATER_THAN_OR_EQUAL);
            else if ("<".equals(op)) pushBiNode(stack, BiNodeToBooleanType.LESS_THAN);
            else if (">".equals(op)) pushBiNode(stack, BiNodeToBooleanType.GREATER_THAN);
            else if ("!".equals(op)) pushUnaryNode(stack, UnaryNodeType.NEGATE);
            else if (":".equals(op)) continue; // NO-OP, all handled by "?"
            else if ("?".equals(op)) pushConditional(stack);
            else if (UNARY_NEGATION.equals(op)) pushUnaryNode(stack, UnaryNodeType.NEGATE);
            else if (isValidLong(op)) {
                long val = parseValidLong(op);
                stack.push(new NodeConstantLong(val));
            } else if (isValidDouble(op)) {
                stack.push(new NodeConstantDouble(Double.parseDouble(op)));
            } else if (BOOLEAN_MATCHER.matcher(op).matches()) {
                stack.push(NodeConstantBoolean.get(Boolean.parseBoolean(op)));
            } else if (STRING_MATCHER.matcher(op).matches()) {
                stack.push(new NodeConstantString(op.substring(1, op.length() - 1)));
            } else if (op.startsWith(FUNCTION_START)) {
                // Its a function
                String function = op.substring(1);
                pushFunctionNode(stack, function, context);
            } else {
                IExpressionNode node = context == null ? null : context.getVariable(op);
                if (node != null) {
                    stack.push(node);
                } else {
                    throw new InvalidExpressionException("Unknown variable '" + op + "'");
                }
            }
        }

        IExpressionNode node = stack.pop().inline();
        if (!stack.isEmpty()) {
            throw new InvalidExpressionException("Tried to make an expression with too many nodes! (" + stack + ")");
        }
        return node;
    }

    public static boolean isValidDouble(String op) {
        return DOUBLE_MATCHER.matcher(op).matches();
    }

    public static boolean isValidLong(String value) {
        return LONG_MATCHER.matcher(value).matches();
    }

    public static long parseValidLong(String value) {
        long val;
        if (value.startsWith("0x")) {
            // its a hexadecimal number
            String v = value.substring(2).replace("_", "");
            val = Long.parseLong(v, 16);
        } else {
            val = Long.parseLong(value);
        }
        return val;
    }

    public static IExpressionNode convertBinary(IExpressionNode convert, IExpressionNode compare) throws InvalidExpressionException {
        if (convert instanceof INodeDouble) {
            if (compare instanceof INodeDouble) {
                return convert;
            } else if (compare instanceof INodeLong) {
                return convert;
            } else if (compare instanceof INodeString) {
                return new NodeCastDoubleToString((INodeDouble) convert);
            } else {
                throw new InvalidExpressionException("Cannot convert " + convert + " with " + compare);
            }
        } else if (convert instanceof INodeLong) {
            if (compare instanceof INodeDouble) {
                return new NodeCastLongToDouble((INodeLong) convert);
            } else if (compare instanceof INodeLong) {
                return convert;
            } else if (compare instanceof INodeString) {
                return new NodeCastLongToString((INodeLong) convert);
            } else {
                throw new InvalidExpressionException("Cannot convert " + convert + " with " + compare);
            }
        } else if (convert instanceof INodeString) {
            return convert;
        } else if (convert instanceof INodeBoolean) {
            if (compare instanceof INodeBoolean) {
                return convert;
            } else if (compare instanceof INodeString) {
                return new NodeCastBooleanToString((INodeBoolean) convert);
            } else {
                throw new InvalidExpressionException("Cannot convert " + convert + " with " + compare);
            }
        } else {
            throw new InvalidExpressionException("Unknown type " + convert);
        }
    }

    private static void pushBiNode(NodeStack stack, IBinaryNodeType type) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();
        stack.push(type.createNode(left, right));
    }

    private static void pushUnaryNode(NodeStack stack, IUnaryNodeType type) throws InvalidExpressionException {
        IExpressionNode node = stack.pop();
        if (node instanceof INodeLong) {
            stack.push(type.createLongNode((INodeLong) node));
        } else if (node instanceof INodeDouble) {
            stack.push(type.createDoubleNode((INodeDouble) node));
        } else if (node instanceof INodeBoolean) {
            stack.push(type.createBooleanNode((INodeBoolean) node));
        } else if (node instanceof INodeString) {
            stack.push(type.createStringNode((INodeString) node));
        } else {
            throw new InvalidExpressionException("Unknown node " + node);
        }
    }

    private static void pushConditional(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();
        IExpressionNode conditional = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (conditional instanceof INodeBoolean) {
            INodeBoolean condition = (INodeBoolean) conditional;
            if (right instanceof INodeBoolean) {
                stack.push(new NodeConditionalBoolean(condition, (INodeBoolean) left, (INodeBoolean) right));
            } else if (right instanceof INodeDouble) {
                stack.push(new NodeConditionalDouble(condition, (INodeDouble) left, (INodeDouble) right));
            } else if (right instanceof INodeString) {
                stack.push(new NodeConditionalString(condition, (INodeString) left, (INodeString) right));
            } else if (right instanceof INodeLong) {
                stack.push(new NodeConditionalLong(condition, (INodeLong) left, (INodeLong) right));
            } else {
                throw new InvalidExpressionException("Unknown node " + left);
            }

        } else {
            throw new InvalidExpressionException("Required a boolean node, but got '" + conditional + "' of " + conditional.getClass());
        }
    }

    private static void pushFunctionNode(NodeStack stack, String function, FunctionContext context) throws InvalidExpressionException {
        String name = function.substring(0, function.indexOf(FUNCTION_ARGS));
        String argCount = function.substring(function.indexOf(FUNCTION_ARGS) + 1);
        int count = Integer.parseInt(argCount);

        if (name.startsWith(".")) {
            /* Allow object style function calling by making the called node be the first argument to the function,
             * pushing all other nodes back */
            name = name.substring(1);
            count++;
        }

        INodeFunc func = context.getFunction(name, count);

        if (func == null) {
            throw new InvalidExpressionException("Unknown function '" + name + "'");
        }

        NodeStackRecording recorder = new NodeStackRecording();
        func.getNode(recorder);

        if (recorder.types.size() != count) {
            throw new InvalidExpressionException("The function " + name + " takes " + recorder.types + " but only " + count + " were given!");
        }

        stack.setRecorder(recorder.types, func);
        IExpressionNode node = func.getNode(stack);
        stack.checkAndRemoveRecorder();

        stack.push(node);
    }
}
