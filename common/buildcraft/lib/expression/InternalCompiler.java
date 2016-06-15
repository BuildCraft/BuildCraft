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
import buildcraft.lib.expression.node.cast.NodeCastBooleanToString;
import buildcraft.lib.expression.node.cast.NodeCastDoubleToString;
import buildcraft.lib.expression.node.cast.NodeCastLongToDouble;
import buildcraft.lib.expression.node.cast.NodeCastLongToString;
import buildcraft.lib.expression.node.simple.*;

class InternalCompiler {
    private static final String UNARY_NEGATION = "¬";
    private static final String FUNCTION_START = "@";
    private static final String FUNCTION_ARGS = "#";
    private static final String OPERATORS = "+-*/^%~ << >> == <= >=";
    private static final String splitters = OPERATORS + "(),";
    private static final String leftAssosiative = "+-^*/%";
    private static final String rightAssosiative = "";
    private static final String[] precedence = { "(),", "== <= >=", "<< >>", "+-", "%", "*/", "^", "~¬" };

    /** This is not a complete encompassing regular expression, just for the char set that can be used */
    private static final String expressionRegex = "[a-z0-9]|[+\\-*/^%()~,\\._<=>\"]";
    private static final Pattern expressionMatcher = Pattern.compile(expressionRegex);

    private static final String LONG_REGEX = "[-+]?[0-9]+";
    private static final String DOUBLE_REGEX = "[-+]?[0-9]+(\\.[0-9]+)?";
    private static final String BOOLEAN_REGEX = "true|false";
    private static final String STRING_REGEX = "\"[a-z_]*\"";

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
            return type.name().toLowerCase(Locale.ROOT) + " " + name;
        }
    }

    public static Pair<IExpressionNode, ArgumentCounts> compileExpression(String expression, IFunctionMap functions) throws InvalidExpressionException {
        expression = expression.replace(" ", "").toLowerCase(Locale.ROOT);
        Argument[] args = parseArgs(expression);
        System.out.println(Arrays.toString(args));
        String actualExpression = extractExpression(expression);
        actualExpression = validateExpression(actualExpression);
        String[] infix = split(actualExpression);
        System.out.println(Arrays.toString(infix));
        String[] postfix = convertToPostfix(infix);
        System.out.println(Arrays.toString(postfix));
        return makeExpression(args, postfix, functions);
    }

    private static Argument[] parseArgs(String expression) throws InvalidExpressionException {
        List<Argument> args = new ArrayList<>();
        if (expression.startsWith("{") && expression.contains("}")) {
            String inMiddle = expression.substring(1, expression.indexOf("}"));
            System.out.println("inMiddle = " + inMiddle);
            String[] strings = inMiddle.split(",");
            System.out.println("argTypes = " + Arrays.toString(strings));
            for (int i = 0; i < strings.length; i++) {
                String type = strings[i];
                if (type.startsWith("long")) {
                    String val = type.substring(4);
                    args.add(new Argument(val, ArgType.LONG));
                } else if (type.startsWith("double")) {
                    String val = type.substring(6);
                    args.add(new Argument(val, ArgType.DOUBLE));
                } else {
                    throw new InvalidExpressionException("Unknown type " + type);
                }
            }
        }
        return args.toArray(new Argument[args.size()]);
    }

    private static String extractExpression(String expression) {
        if (expression.startsWith("{") && expression.contains("}")) {
            String actual = expression.substring(expression.indexOf("}") + 1);
            System.out.println(actual);
            return actual;
        }
        return expression;
    }

    private static String validateExpression(String expression) throws InvalidExpressionException {
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!expressionMatcher.matcher(c + "").matches()) {
                throw new InvalidExpressionException("Could not compile " + expression + ", as the " + i + "th char ('" + c + "') was invalid");
            }
        }
        return expression;
    }

    private static String[] split(String function) {
        function = function.replaceAll("\\s", "");
        List<String> list = Lists.newArrayList();
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < function.length(); index++) {
            char toTest = function.charAt(index);
            if (splitters.indexOf(toTest) != -1) {
                if (buffer.length() > 0) {
                    list.add(buffer.toString());
                }
                list.add(String.valueOf(toTest));
                buffer = new StringBuffer();
            } else {
                buffer.append(toTest);
            }
        }
        if (buffer.length() > 0) {
            list.add(buffer.toString());
        }
        return list.toArray(new String[list.size()]);
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
        System.out.println("Converting " + Arrays.toString(infix));
        System.out.println("         Stack=" + stack + ", postfix=" + postfix);
        boolean justPushedFunc = false;
        for (index = 0; index < infix.length; index++) {
            String token = infix[index];
            System.out.println("  - Token \"" + token + "\"");

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
            } else if (OPERATORS.contains(token)) {
                // Its an operator
                if ("-".equals(token) && (index == 0 || (OPERATORS + "(,").contains(infix[index - 1]))) {
                    token = UNARY_NEGATION;
                }

                String s;
                while ((s = stack.peek()) != null) {
                    int tokenPrec = getPrecendence(token);
                    int stackPrec = getPrecendence(s);
                    boolean shouldContinue = leftAssosiative.contains(token) && tokenPrec <= stackPrec;
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
            System.out.println("         Stack=" + stack + ", postfix=" + postfix);
        }

        while (!stack.isEmpty()) {
            String operator = stack.pop();
            System.out.println("  - Operator \"" + operator + "\"");
            if ("(".equals(operator)) {
                throw new InvalidExpressionException("Too many opening parenthesis!");
            } else if (")".equals(operator)) {
                throw new InvalidExpressionException("Too many closing parenthesis!");
            } else {
                postfix.add(operator);
            }
            System.out.println("         Stack=" + stack + ", postfix=" + postfix);
        }

        return postfix.toArray(new String[postfix.size()]);
    }

    private static Pair<IExpressionNode, ArgumentCounts> makeExpression(Argument[] args, String[] postfix, IFunctionMap functions) throws InvalidExpressionException {
        Deque<IExpressionNode> stack = Queues.newArrayDeque();
        for (String op : postfix) {
            if ("-".equals(op)) pushSubtraction(stack);
            else if ("+".equals(op)) pushAddition(stack);
            else if ("*".equals(op)) pushMultiply(stack);
            else if ("/".equals(op)) pushDivide(stack);
            else if ("%".equals(op)) pushModulus(stack);
            else if ("^".equals(op)) pushPower(stack);
            else if ("~".equals(op)) pushBitwiseInvert(stack);
            else if (UNARY_NEGATION.equals(op)) pushNegation(stack);
            else if (LONG_MATCHER.matcher(op).matches()) {
                stack.push(new NodeValueLong(Long.parseLong(op)));
            } else if (DOUBLE_MATCHER.matcher(op).matches()) {
                stack.push(new NodeValueDouble(Double.parseDouble(op)));
            } else if (op.startsWith(FUNCTION_START)) {
                // Its a function
                String function = op.substring(1);
                pushFunctionNode(stack, function, functions);
            } else {
                boolean found = false;
                int i = 0;
                for (Argument arg : args) {
                    if (arg.name.equals(op)) {
                        pushArgumentNode(stack, i, arg.type);
                        found = true;
                    }
                    i++;
                }
                if (!found) {
                    throw new InvalidExpressionException("Unknown argument \"" + op + "\"");
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
                System.out.println("Checking " + name + "(" + e2.getCounts() + ")");
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
