package buildcraft.lib.expression;

import java.util.*;
import java.util.regex.Pattern;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.binary.*;
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
import buildcraft.lib.expression.node.unary.NodeBooleanInvert;
import buildcraft.lib.expression.node.unary.NodeUnaryDouble;
import buildcraft.lib.expression.node.unary.NodeUnaryLong;
import buildcraft.lib.expression.node.value.*;

public class InternalCompiler {
    private static final String UNARY_NEGATION = "¬";
    private static final String FUNCTION_START = "@";
    private static final String FUNCTION_ARGS = "#";
    private static final String OPERATORS = "+-*/^%~?:& << >> == <= >= && || !=";
    private static final String leftAssosiative = "+-^*/%||&&==!=<=>=<<>>?&";
    private static final String rightAssosiative = "";
    private static final String[] precedence = { "(),", "?", "|| &&", "!= == <= >=", "&", "<< >>", "+-", "%", "*/", "^", "~¬" };

    private static final String LONG_REGEX = "[-+]?[0-9]+";
    private static final String DOUBLE_REGEX = "[-+]?[0-9]+(\\.[0-9]+)?";
    private static final String BOOLEAN_REGEX = "true|false";
    private static final String STRING_REGEX = "'.*'";

    private static final Pattern LONG_MATCHER = Pattern.compile(LONG_REGEX);
    private static final Pattern DOUBLE_MATCHER = Pattern.compile(DOUBLE_REGEX);
    private static final Pattern BOOLEAN_MATCHER = Pattern.compile(BOOLEAN_REGEX);
    private static final Pattern STRING_MATCHER = Pattern.compile(STRING_REGEX);
    
    public static IExpressionNode compileExpression(String expression, FunctionContext context) throws InvalidExpressionException {
        ExpressionDebugManager.debugPrintln("Compiling " + expression);
        String[] infix = tokenize(expression);
        String[] postfix = convertToPostfix(infix);
        ExpressionDebugManager.debugPrintln(Arrays.toString(postfix));
        return makeExpression(postfix, context);
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

    private static String[] tokenize(String function) {
        return TokeniserDefaults.createTokensizer().tokenize(function);
    }

    private static int getPrecendence(String token) {
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
                    if (s.equals("?")) {
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
                    int tokenPrec = getPrecendence(token);
                    int stackPrec = getPrecendence(s);
                    boolean continueIfEqual = !"?".contains(token);

                    boolean shouldContinue = leftAssosiative.contains(token) && (continueIfEqual ? tokenPrec <= stackPrec : tokenPrec < stackPrec);
                    if (!shouldContinue && rightAssosiative.contains(token)) {
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
            if ("-".equals(op)) pushSubtraction(stack);
            else if ("+".equals(op)) pushAddition(stack);
            else if ("&".equals(op)) pushStringConcatenate(stack);
            else if ("*".equals(op)) pushMultiply(stack);
            else if ("/".equals(op)) pushDivide(stack);
            else if ("%".equals(op)) pushModulus(stack);
            else if ("^".equals(op)) pushPower(stack);
            else if ("~".equals(op)) pushBitwiseInvert(stack);
            else if ("==".equals(op)) pushEqual(stack);
            else if ("<=".equals(op)) pushLessOrEqual(stack);
            else if (">=".equals(op)) pushGreaterOrEqual(stack);
            else if ("<".equals(op)) pushLess(stack);
            else if (">".equals(op)) pushGreater(stack);
            else if ("!=".equals(op)) pushNotEqual(stack);
            else if ("&&".equals(op)) pushBooleanBi(stack, NodeBinaryBoolean.Type.AND);
            else if ("||".equals(op)) pushBooleanBi(stack, NodeBinaryBoolean.Type.OR);
            else if ("!".equals(op)) pushBooleanNot(stack);
            else if (":".equals(op)) ; // NO-OP, all handled by "?"
            else if ("?".equals(op)) pushConditional(stack);
            else if (UNARY_NEGATION.equals(op)) pushNegation(stack);
            else if (LONG_MATCHER.matcher(op).matches()) {
                stack.push(new NodeConstantLong(Long.parseLong(op)));
            } else if (DOUBLE_MATCHER.matcher(op).matches()) {
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

    private static void pushSubtraction(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.SUB.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.SUB.create((INodeLong) left, (INodeLong) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushAddition(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.ADD.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.ADD.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeString) {
            stack.push(new NodeConcatenateString((INodeString) left, (INodeString) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushStringConcatenate(NodeStack stack) throws InvalidExpressionException {
        INodeString right = stack.popString();
        INodeString left = stack.popString();
        stack.push(new NodeConcatenateString(left, right));
    }

    private static void pushMultiply(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.MUL.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.MUL.create((INodeLong) left, (INodeLong) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushDivide(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.DIV.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.DIV.create((INodeLong) left, (INodeLong) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushModulus(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.MOD.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.MOD.create((INodeLong) left, (INodeLong) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushPower(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.POW.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.POW.create((INodeLong) left, (INodeLong) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static IExpressionNode convertBinary(IExpressionNode convert, IExpressionNode compare) throws InvalidExpressionException {
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

    private static void pushBitwiseInvert(NodeStack stack) throws InvalidExpressionException {
        stack.push(NodeUnaryLong.Type.BITWISE_INVERT.create(stack.popLong()));
    }

    private static void pushEqual(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeLong) {
            stack.push(NodeBinaryLongToBoolean.Type.EQUAL.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDoubleToBoolean.Type.EQUAL.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeString) {
            stack.push(new NodeEqualityString((INodeString) left, (INodeString) right));
        } else if (left instanceof INodeBoolean) {
            stack.push(NodeBinaryBoolean.Type.EQUAL.create((INodeBoolean) left, (INodeBoolean) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushLessOrEqual(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeLong) {
            stack.push(NodeBinaryLongToBoolean.Type.LESS_THAN_OR_EQUAL.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDoubleToBoolean.Type.LESS_THAN_OR_EQUAL.create((INodeDouble) left, (INodeDouble) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushGreaterOrEqual(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeLong) {
            stack.push(NodeBinaryLongToBoolean.Type.GREATER_THAN_OR_EQUAL.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDoubleToBoolean.Type.GREATER_THAN_OR_EQUAL.create((INodeDouble) left, (INodeDouble) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushLess(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeLong) {
            stack.push(NodeBinaryLongToBoolean.Type.LESS_THAN.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDoubleToBoolean.Type.LESS_THAN.create((INodeDouble) left, (INodeDouble) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushGreater(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeLong) {
            stack.push(NodeBinaryLongToBoolean.Type.GREATER_THAN.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDoubleToBoolean.Type.GREATER_THAN.create((INodeDouble) left, (INodeDouble) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushNotEqual(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeLong) {
            stack.push(NodeBinaryLongToBoolean.Type.NOT_EQUAL.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDoubleToBoolean.Type.NOT_EQUAL.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeString) {
            stack.push(new NodeBooleanInvert(new NodeEqualityString((INodeString) left, (INodeString) right)));
        } else if (left instanceof INodeBoolean) {
            stack.push(NodeBinaryBoolean.Type.NOT_EQUAL.create((INodeBoolean) left, (INodeBoolean) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushBooleanBi(NodeStack stack, NodeBinaryBoolean.Type type) throws InvalidExpressionException {
        INodeBoolean right = stack.popBoolean();
        INodeBoolean left = stack.popBoolean();
        stack.push(type.create(left, right));
    }

    private static void pushBooleanNot(NodeStack stack) throws InvalidExpressionException {
        stack.push(new NodeBooleanInvert(stack.popBoolean()));
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
                throw new InvalidExpressionException("Unkown node " + left);
            }

        } else {
            throw new InvalidExpressionException("Required a boolean node, but got '" + conditional + "' of " + conditional.getClass());
        }
    }

    private static void pushNegation(NodeStack stack) throws InvalidExpressionException {
        IExpressionNode node = stack.pop();
        if (node instanceof INodeDouble) {
            stack.push(NodeUnaryDouble.Type.NEG.create((INodeDouble) node));
        } else if (node instanceof INodeLong) {
            stack.push(NodeUnaryLong.Type.NEG.create((INodeLong) node));
        } else {
            throw new InvalidExpressionException("Unknown node " + node);
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
