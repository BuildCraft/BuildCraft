package buildcraft.lib.expression;

import java.util.*;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import org.apache.commons.lang3.tuple.Pair;

import buildcraft.lib.expression.api.*;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.arg.NodeArgumentDouble;
import buildcraft.lib.expression.node.arg.NodeArgumentLong;
import buildcraft.lib.expression.node.binary.*;
import buildcraft.lib.expression.node.cast.NodeCastBooleanToString;
import buildcraft.lib.expression.node.cast.NodeCastDoubleToString;
import buildcraft.lib.expression.node.cast.NodeCastLongToDouble;
import buildcraft.lib.expression.node.cast.NodeCastLongToString;
import buildcraft.lib.expression.node.condition.NodeConditionalBoolean;
import buildcraft.lib.expression.node.condition.NodeConditionalDouble;
import buildcraft.lib.expression.node.condition.NodeConditionalLong;
import buildcraft.lib.expression.node.condition.NodeConditionalString;
import buildcraft.lib.expression.node.unary.NodeBooleanInvert;
import buildcraft.lib.expression.node.unary.NodeUnaryDouble;
import buildcraft.lib.expression.node.unary.NodeUnaryLong;
import buildcraft.lib.expression.node.value.NodeImmutableBoolean;
import buildcraft.lib.expression.node.value.NodeImmutableDouble;
import buildcraft.lib.expression.node.value.NodeImmutableLong;
import buildcraft.lib.expression.node.value.NodeImmutableString;

class InternalCompiler {
    private static final String UNARY_NEGATION = "¬";
    private static final String FUNCTION_START = "@";
    private static final String FUNCTION_ARGS = "#";
    private static final String OPERATORS = "+-*/^%~?: << >> == <= >= && || !=";
    private static final String leftAssosiative = "+-^*/%||&&==!=<=>=<<>>?";
    private static final String rightAssosiative = "";
    private static final String[] precedence = { "(),", "?", "|| &&", "!= == <= >=", "<< >>", "+-", "%", "*/", "^", "~¬" };

    private static final String LONG_REGEX = "[-+]?[0-9]+";
    private static final String DOUBLE_REGEX = "[-+]?[0-9]+(\\.[0-9]+)?";
    private static final String BOOLEAN_REGEX = "true|false";
    private static final String STRING_REGEX = "'.*'";

    private static final Pattern LONG_MATCHER = Pattern.compile(LONG_REGEX);
    private static final Pattern DOUBLE_MATCHER = Pattern.compile(DOUBLE_REGEX);
    private static final Pattern BOOLEAN_MATCHER = Pattern.compile(BOOLEAN_REGEX);
    private static final Pattern STRING_MATCHER = Pattern.compile(STRING_REGEX);

    private static class Argument {
        public final String name;
        public final ArgType type;

        public Argument(String name, ArgType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return type.name().toLowerCase(Locale.ROOT) + " '" + name + "'";
        }
    }

    public static Pair<IExpressionNode, ArgumentCounts> compileExpression(String expression, FunctionContext context) throws InvalidExpressionException {
        expression = expression.toLowerCase(Locale.ROOT);
        Argument[] args = parseArgs(expression);
        GenericExpressionCompiler.debugPrintln(Arrays.toString(args));
        String actualExpression = extractExpression(expression);
        String[] infix = split(actualExpression);
        GenericExpressionCompiler.debugPrintln(Arrays.toString(infix));
        String[] postfix = convertToPostfix(infix);
        GenericExpressionCompiler.debugPrintln(Arrays.toString(postfix));
        return makeExpression(args, postfix, context);
    }

    private static Argument[] parseArgs(String expression) throws InvalidExpressionException {
        List<Argument> args = new ArrayList<>();
        if (expression.startsWith("{") && expression.contains("}")) {
            String inMiddle = expression.substring(1, expression.indexOf("}"));
            GenericExpressionCompiler.debugPrintln("inMiddle = " + inMiddle);
            String[] strings = inMiddle.split(",");
            GenericExpressionCompiler.debugPrintln("argTypes = " + Arrays.toString(strings));
            for (int i = 0; i < strings.length; i++) {
                String type = strings[i].trim();
                if (type.startsWith("long ")) {
                    String val = type.substring(5);
                    args.add(new Argument(val.trim(), ArgType.LONG));
                } else if (type.startsWith("double ")) {
                    String val = type.substring(7);
                    args.add(new Argument(val.trim(), ArgType.DOUBLE));
                } else {
                    throw new InvalidExpressionException("Unknown type '" + type + "'");
                }
            }
        }
        return args.toArray(new Argument[args.size()]);
    }

    private static String extractExpression(String expression) {
        if (expression.startsWith("{") && expression.contains("}")) {
            String actual = expression.substring(expression.indexOf("}") + 1);
            GenericExpressionCompiler.debugPrintln(actual);
            return actual;
        }
        return expression;
    }

    private static String[] split(String function) {
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
        Deque<String> stack = Queues.newArrayDeque();
        List<String> postfix = Lists.newArrayList();
        int index = 0;
        GenericExpressionCompiler.debugPrintln("Converting " + Arrays.toString(infix));
        GenericExpressionCompiler.debugPrintln("         Stack=" + stack + ", postfix=" + postfix);
        boolean justPushedFunc = false;
        for (index = 0; index < infix.length; index++) {
            String token = infix[index];
            GenericExpressionCompiler.debugPrintln("  - Token \"" + token + "\"");

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
            GenericExpressionCompiler.debugPrintln("         Stack=" + stack + ", postfix=" + postfix);
        }

        while (!stack.isEmpty()) {
            String operator = stack.pop();
            GenericExpressionCompiler.debugPrintln("  - Operator \"" + operator + "\"");
            if ("(".equals(operator)) {
                throw new InvalidExpressionException("Too many opening parenthesis!");
            } else if (")".equals(operator)) {
                throw new InvalidExpressionException("Too many closing parenthesis!");
            } else {
                postfix.add(operator);
            }
            GenericExpressionCompiler.debugPrintln("         Stack=" + stack + ", postfix=" + postfix);
        }

        return postfix.toArray(new String[postfix.size()]);
    }

    private static Pair<IExpressionNode, ArgumentCounts> makeExpression(Argument[] args, String[] postfix, FunctionContext context) throws InvalidExpressionException {
        Deque<IExpressionNode> stack = Queues.newArrayDeque();
        for (String op : postfix) {
            if ("-".equals(op)) pushSubtraction(stack);
            else if ("+".equals(op)) pushAddition(stack);
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
            else if ("&&".equals(op)) pushBooleanAnd(stack);
            else if ("||".equals(op)) pushBooleanOr(stack);
            else if ("!".equals(op)) pushBooleanNot(stack);
            else if (":".equals(op)) pushSelector(stack);
            else if ("?".equals(op)) pushConditional(stack);
            else if (UNARY_NEGATION.equals(op)) pushNegation(stack);
            else if (LONG_MATCHER.matcher(op).matches()) {
                stack.push(new NodeImmutableLong(Long.parseLong(op)));
            } else if (DOUBLE_MATCHER.matcher(op).matches()) {
                stack.push(new NodeImmutableDouble(Double.parseDouble(op)));
            } else if (BOOLEAN_MATCHER.matcher(op).matches()) {
                stack.push(NodeImmutableBoolean.get(Boolean.parseBoolean(op)));
            } else if (STRING_MATCHER.matcher(op).matches()) {
                stack.push(new NodeImmutableString(op.substring(1, op.length() - 1)));
            } else if (op.startsWith(FUNCTION_START)) {
                // Its a function
                String function = op.substring(1);
                pushFunctionNode(stack, function, context == null ? null : context.getFunctionMap());
            } else {
                boolean found = false;
                int i = 0;
                for (Argument arg : args) {
                    if (arg.name.equals(op)) {
                        pushArgumentNode(stack, i, arg.type);
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found) {
                    IExpressionNode node = context == null ? null : context.getAny(op);
                    if (node != null) {
                        stack.push(node);
                    } else {
                        throw new InvalidExpressionException("Unknown variable '" + op + "'");
                    }
                }
            }
        }
        if (stack.size() != 1) {
            throw new InvalidExpressionException("Tried to make an expression with too many nodes! (" + stack + ")");
        }

        ArgType[] types = new ArgType[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i].type;
        }
        ArgumentCounts counts = new ArgumentCounts(types);
        return Pair.of(stack.pop().inline(null), counts);
    }

    private static void pushSubtraction(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushAddition(Deque<IExpressionNode> stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeDouble) {
            stack.push(NodeBinaryDouble.Type.ADD.create((INodeDouble) left, (INodeDouble) right));
        } else if (left instanceof INodeLong) {
            stack.push(NodeBinaryLong.Type.ADD.create((INodeLong) left, (INodeLong) right));
        } else if (left instanceof INodeString) {
            stack.push(new NodeAppendString((INodeString) left, (INodeString) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushMultiply(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushDivide(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushModulus(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushPower(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushBitwiseInvert(Deque<IExpressionNode> stack) throws InvalidExpressionException {
        IExpressionNode node = stack.pop();
        if (node instanceof INodeLong) {
            stack.push(NodeUnaryLong.Type.BITWISE_INVERT.create((INodeLong) node));
        } else {
            throw new InvalidExpressionException("Unknown node " + node);
        }
    }

    private static void pushEqual(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushLessOrEqual(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushGreaterOrEqual(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushLess(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushGreater(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushNotEqual(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushBooleanAnd(Deque<IExpressionNode> stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeBoolean) {
            stack.push(NodeBinaryBoolean.Type.AND.create((INodeBoolean) left, (INodeBoolean) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushBooleanOr(Deque<IExpressionNode> stack) throws InvalidExpressionException {
        IExpressionNode right = stack.pop();
        IExpressionNode left = stack.pop();

        right = convertBinary(right, left);
        left = convertBinary(left, right);

        if (left instanceof INodeBoolean) {
            stack.push(NodeBinaryBoolean.Type.OR.create((INodeBoolean) left, (INodeBoolean) right));
        } else {
            throw new InvalidExpressionException("Unknown node " + left + ", " + right);
        }
    }

    private static void pushBooleanNot(Deque<IExpressionNode> stack) throws InvalidExpressionException {
        IExpressionNode node = stack.pop();
        if (node instanceof INodeBoolean) {
            stack.push(new NodeBooleanInvert((INodeBoolean) node));
        } else {
            throw new InvalidExpressionException("Unknown node " + node);
        }
    }

    private static void pushSelector(Deque<IExpressionNode> stack) {
        // NO-OP, is all handled by pushConditional
    }

    private static void pushConditional(Deque<IExpressionNode> stack) throws InvalidExpressionException {
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

    private static void pushNegation(Deque<IExpressionNode> stack) throws InvalidExpressionException {
        IExpressionNode node = stack.pop();
        if (node instanceof INodeDouble) {
            stack.push(NodeUnaryDouble.Type.NEG.create((INodeDouble) node));
        } else if (node instanceof INodeLong) {
            stack.push(NodeUnaryLong.Type.NEG.create((INodeLong) node));
        } else {
            throw new InvalidExpressionException("Unknown node " + node);
        }
    }

    private static void pushFunctionNode(Deque<IExpressionNode> stack, String function, IFunctionMap functions) throws InvalidExpressionException {
        String name = function.substring(0, function.indexOf(FUNCTION_ARGS));
        String argCount = function.substring(function.indexOf(FUNCTION_ARGS) + 1);
        int count = Integer.parseInt(argCount);

        ArgType[] types = new ArgType[count];
        List<IExpressionNode> nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            IExpressionNode node = stack.pop();
            nodes.add(node);
            types[count - i - 1] = typeOf(node);
        }
        Collections.reverse(nodes);

        ArgumentCounts counts = new ArgumentCounts(types);
        Arguments args = counts.createArgs();
        for (IExpressionNode node : nodes) {
            args.appendNode(node);
        }

        FunctionIdentifier identifier = new FunctionIdentifier(name, counts);
        IExpression exp = functions.getExpression(identifier);

        if (exp == null) {
            // Find the next best one that we can cast to/from
            for (IExpression e2 : functions.getExpressions(name, count)) {
                GenericExpressionCompiler.debugPrintln("Checking " + name + "(" + e2.getCounts() + ")");
                if (counts.canCastTo(e2.getCounts())) {
                    args = args.castTo(e2.getCounts());
                    exp = e2;
                    if (args == null) {
                        throw new IllegalStateException("Somehow could cast from " + counts + " to " + e2.getCounts() + " but it didn't work!");
                    }
                }
            }
        }
        if (exp == null) {
            throw new InvalidExpressionException("No function defined for " + identifier);
        }

        stack.push(exp.derive(args));
    }

    private static ArgType typeOf(IExpressionNode node) {
        if (node instanceof INodeLong) return ArgType.LONG;
        if (node instanceof INodeDouble) return ArgType.DOUBLE;
        if (node instanceof INodeBoolean) return ArgType.BOOL;
        /* if (node instanceof INodeString) */ return ArgType.STRING;
    }

    private static void pushArgumentNode(Deque<IExpressionNode> stack, int index, ArgType type) throws InvalidExpressionException {
        if (type == ArgType.LONG) {
            stack.push(new NodeArgumentLong(index));
        } else if (type == ArgType.DOUBLE) {
            stack.push(new NodeArgumentDouble(index));
        } else {
            throw new InvalidExpressionException("Unknown type " + type);
        }
    }
}
