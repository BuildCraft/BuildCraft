package buildcraft.lib.expression;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class ExpressionCompiler {
    private static String operators = "+-*/^%~";
    private static String splitters = operators + "(),";
    private static String leftAssosiative = "+-^*/%";
    private static String rightAssosiative = "-";
    private static String[] precedence = { "()", "+-", "%", "*/", "^", "~" };
    /** This is not a complete encompassing regular expression, just for the char set that can be used */
    private static String expressionRegex = "[a-z0-9]|[+\\-*/^%()~]";
    private static Pattern expressionMatcher = Pattern.compile(expressionRegex);
    private static String numberRegex = "[-+]?[0-9]+";
    private static Pattern numberMatcher = Pattern.compile(numberRegex);

    public static Expression compileExpression(String expression) throws InvalidExpressionException {
        return compileExpression(expression, null);
    }

    public static Expression compileExpression(String expression, Map<String, Expression> functions) throws InvalidExpressionException {
        expression = expression.replace(" ", "");
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!expressionMatcher.matcher(c + "").matches()) {
                throw new InvalidExpressionException("Could not compile " + expression + ", as the " + i + "th char ('" + c + "') was invalid");
            }
        }
        String[] split = split(expression);
        String[] postfix = convertToPostfix(split);
        return makeExpression(postfix);
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
        if (token.startsWith("ƒ")) {
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
        for (index = 0; index < infix.length; index++) {
            String token = infix[index];
            if (numberMatcher.matcher(token).matches()) {
                // Its a number
                postfix.add(token);
            } else if (",".equals(token)) {
                boolean found = false;
                while (!stack.isEmpty()) {
                    String fromStack = stack.pop();
                    if ("(".equals(fromStack) || fromStack.startsWith("ƒ")) {
                        found = true;
                        stack.push(fromStack);
                        break;
                    } else {
                        postfix.add(fromStack);
                    }
                }
                if (!found) {
                    throw new InvalidExpressionException("Did not find an opening parenthesis for the comma!");
                }
            } else if ("(".equals(token)) {
                stack.push(token);
            } else if (")".equals(token)) {
                boolean found = false;
                while (!stack.isEmpty()) {
                    String fromStack = stack.pop();
                    if ("(".equals(fromStack)) {
                        found = true;
                        break;
                    } else if (fromStack.startsWith("ƒ")) {
                        found = true;
                        // Add it back onto the stack to be used later
                        postfix.add(fromStack);
                        break;
                    } else {
                        postfix.add(fromStack);
                    }
                }
                if (!found) {
                    throw new InvalidExpressionException("Too many closing parenthesis!");
                }
            } else if (operators.contains(token)) {
                // Its an operator
                if ("-".equals(token) && (index == 0 || "(,:-".contains(infix[index - 1]))) {
                    // Bit ugly, but we use a tilde for negative numbers
                    token = "~";
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
                // Its a function (The next token is an open parenthesis)
                // Prefix it with \u0192, and push it to the stack
                stack.push("ƒ" + token);
                // Also ignore the parenthesis (the function is treated as if it was an open parenthesis)
                index++;
            } else {
                // Assume it is a variable, so its treated as if it was a static number
                postfix.add(token);
            }
        }

        while (!stack.isEmpty()) {
            String operator = stack.pop();
            if ("(".equals(operator)) {
                throw new InvalidExpressionException("Too many opening parenthesis!");
            } else if (")".equals(operator)) {
                throw new InvalidExpressionException("Too many closing parenthesis!");
            } else {
                postfix.add(operator);
            }
        }

        return postfix.toArray(new String[postfix.size()]);
    }

    private static Expression makeExpression(String[] postfix) throws InvalidExpressionException {
        Map<String, Integer> variableMap = Maps.newHashMap();
        int currentVar = 0;
        Deque<Node> stack = Queues.newArrayDeque();
        for (String op : postfix) {
            if ("-".equals(op)) pushBinaryOperatorNode(stack, (a, b) -> b - a);
            else if ("+".equals(op)) pushBinaryOperatorNode(stack, (a, b) -> b + a);
            else if ("*".equals(op)) pushBinaryOperatorNode(stack, (a, b) -> b * a);
            else if ("/".equals(op)) pushBinaryOperatorNode(stack, (a, b) -> b / a);
            else if ("%".equals(op)) pushBinaryOperatorNode(stack, (a, b) -> b % a);
            else if ("^".equals(op)) pushBinaryOperatorNode(stack, (a, b) -> (long) Math.pow(b, a));
            else if ("~".equals(op)) pushUnaryOperatorNode(stack, (a) -> -a);
            else if (numberMatcher.matcher(op).matches()) {
                stack.push(new ValueNode(Long.parseLong(op)));
            } else if (op.startsWith("ƒ")) {
                // Its a function
                // String function = op.substring(1);
                throw new IllegalStateException("Cannot handle functions yet!");
            } else {
                int index = 0;
                if (variableMap.containsKey(op)) {
                    index = variableMap.get(op);
                } else {
                    index = currentVar++;
                    variableMap.put(op, index);
                }
                stack.push(new VariableNode(index));
            }
        }
        if (stack.size() != 1) {
            throw new InvalidExpressionException("Tried to make an expression with too many nodes! (" + stack + ")");
        }
        Node n = stack.pop();
        return new Expression(n, variableMap);
    }

    private static void pushBinaryOperatorNode(Deque<Node> stack, LongBinaryOperator op) throws InvalidExpressionException {
        if (stack.size() < 2) throw new InvalidExpressionException("Could not pop 2 values from the stack!");
        Node a = stack.pop();
        Node b = stack.pop();
        stack.push(new BinaryExpressionNode(a, b, op));
    }

    private static void pushUnaryOperatorNode(Deque<Node> stack, LongUnaryOperator op) throws InvalidExpressionException {
        if (stack.size() < 1) throw new InvalidExpressionException("Could not pop a value from the stack!");
        Node a = stack.pop();
        stack.push(new UnaryExpressionNode(a, op));
    }

    static abstract class Node {
        abstract long evaluate(long[] variables);
    }

    private static class BinaryExpressionNode extends Node {
        final Node a, b;
        final LongBinaryOperator function;

        public BinaryExpressionNode(Node a, Node b, LongBinaryOperator function) {
            this.a = a;
            this.b = b;
            this.function = function;
        }

        @Override
        long evaluate(long[] variables) {
            long aV = a.evaluate(variables);
            long bV = b.evaluate(variables);
            return function.applyAsLong(aV, bV);
        }
    }

    private static class UnaryExpressionNode extends Node {
        final Node a;
        final LongUnaryOperator function;

        public UnaryExpressionNode(Node a, LongUnaryOperator function) {
            this.a = a;
            this.function = function;
        }

        @Override
        long evaluate(long[] variables) {
            long aV = a.evaluate(variables);
            return function.applyAsLong(aV);
        }
    }

    private static class ValueNode extends Node {
        final long value;

        public ValueNode(long value) {
            this.value = value;
        }

        @Override
        long evaluate(long[] variables) {
            return value;
        }
    }

    private static class VariableNode extends Node {
        final int index;

        public VariableNode(int index) {
            this.index = index;
        }

        @Override
        long evaluate(long[] variables) {
            return variables[index];
        }
    }

    @SuppressWarnings("serial")
    public static class InvalidExpressionException extends Exception {
        public InvalidExpressionException(String message) {
            super(message);
        }
    }
}
