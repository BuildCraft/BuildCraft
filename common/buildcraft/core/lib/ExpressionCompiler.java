package buildcraft.core.lib;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class ExpressionCompiler {
    private static String operators = "+-*/^~";
    private static String splitters = "+-*/^()~,";
    private static String leftAssosiative = "+-*/^";
    private static String rightAssosiative = "~";
    private static String[] precedence = { "()", "+-", "*/", "^", "~" };
    /** This is not a complete encompassing regular expression, just for the char set that can be used */
    private static String expressionRegex = "[a-z0-9]|[+-*/^()~]";
    private static Pattern expressionMather = Pattern.compile(expressionRegex);
    private static String numberRegex = "[-+]?[0-9]*\\.?[0-9]+";
    private static Pattern decimalMatcher = Pattern.compile(numberRegex);

    public static Expression compileExpression(String expression) throws InvalidExpressionException {
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!expressionMather.matcher(c + "").matches()) {
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
            if (decimalMatcher.matcher(token).matches()) {
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
                String s;
                if ("-".equals(token) && (index == 0 || splitters.contains(infix[index - 1]))) {
                    // Bit ugly, but we use a tilde for negative numbers
                    token = "~";
                }

                while ((s = stack.peek()) != null) {
                    int o1 = getPrecendence(token);
                    int o2 = getPrecendence(s);
                    boolean shouldContinue = leftAssosiative.contains(token) && o1 <= o2;
                    if (!shouldContinue) {
                        shouldContinue = rightAssosiative.contains(token) && o1 < o2;
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
            if ("-".equals(op)) {
                Node a = stack.pop();
                Node b = stack.pop();
                stack.push(new DoubleExpressionNode(a, b) {
                    @Override
                    double apply(double a, double b) {
                        return b - a;
                    }
                });
            } else if ("+".equals(op)) {
                Node a = stack.pop();
                Node b = stack.pop();
                stack.push(new DoubleExpressionNode(a, b) {

                    @Override
                    double apply(double a, double b) {
                        return a + b;
                    }
                });
            } else if ("*".equals(op)) {
                Node a = stack.pop();
                Node b = stack.pop();
                stack.push(new DoubleExpressionNode(a, b) {
                    @Override
                    double apply(double a, double b) {
                        return a * b;
                    }
                });
            } else if ("/".equals(op)) {
                Node a = stack.pop();
                Node b = stack.pop();
                stack.push(new DoubleExpressionNode(a, b) {
                    @Override
                    double apply(double a, double b) {
                        return b / a;
                    }
                });
            } else if ("~".equals(op)) {
                Node a = stack.pop();
                stack.push(new SingleExpressionNode(a) {
                    @Override
                    double apply(double a) {
                        return -a;
                    }
                });
            } else if (decimalMatcher.matcher(op).matches()) {
                stack.push(new ValueNode(Double.valueOf(op)));
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
            throw new IllegalArgumentException("Tried to make an expression with too many nodes! (" + stack + ")");
        }
        Node n = stack.pop();
        return new Expression(n, variableMap);
    }

    private static abstract class Node {
        abstract double evaluate(double[] variables);
    }

    public static class Expression {
        public class Variable {
            private final int index;

            private Variable(int index) {
                this.index = index;
            }

            public double get() {
                return variables[index];
            }

            public void set(double value) {
                variables[index] = value;
            }
        }

        private final Node node;
        private final double[] variables;
        private final Map<String, Variable> varAccessor;

        public Expression(Node node, Map<String, Integer> vars) {
            this.node = node;
            this.variables = new double[vars.size()];
            this.varAccessor = Maps.newHashMap();
            for (Entry<String, Integer> entry : vars.entrySet()) {
                varAccessor.put(entry.getKey(), new Variable(entry.getValue()));
            }
        }

        public Map<String, Variable> getVariables() {
            return Collections.unmodifiableMap(varAccessor);
        }

        public Variable getVariable(String name) {
            return varAccessor.get(name);
        }

        public double evaluate() {
            return node.evaluate(variables);
        }
    }

    private static abstract class DoubleExpressionNode extends Node {
        final Node a, b;

        public DoubleExpressionNode(Node a, Node b) {
            this.a = a;
            this.b = b;
        }

        @Override
        double evaluate(double[] variables) {
            double aV = a.evaluate(variables);
            double bV = b.evaluate(variables);
            return apply(aV, bV);
        }

        abstract double apply(double a, double b);
    }

    private static abstract class SingleExpressionNode extends Node {
        final Node a;

        public SingleExpressionNode(Node a) {
            this.a = a;
        }

        @Override
        double evaluate(double[] variables) {
            double aV = a.evaluate(variables);
            return apply(aV);
        }

        abstract double apply(double a);
    }

    private static class ValueNode extends Node {
        final double value;

        public ValueNode(double value) {
            this.value = value;
        }

        @Override
        double evaluate(double[] variables) {
            return value;
        }
    }

    private static class VariableNode extends Node {
        final int index;

        public VariableNode(int index) {
            this.index = index;
        }

        @Override
        double evaluate(double[] variables) {
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
