/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ct.buildcraft.lib.expression.Tokenizer.ITokenizingContext;
import ct.buildcraft.lib.expression.Tokenizer.ResultConsume;
import ct.buildcraft.lib.expression.Tokenizer.TokenResult;
import ct.buildcraft.lib.expression.api.IExpressionNode;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import ct.buildcraft.lib.expression.api.INodeFunc;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import ct.buildcraft.lib.expression.api.INodeStack;
import ct.buildcraft.lib.expression.api.IVariableNode;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
import ct.buildcraft.lib.expression.api.NodeTypes;
import ct.buildcraft.lib.expression.node.cast.NodeCastLongToDouble;
import ct.buildcraft.lib.expression.node.cast.NodeCasting;
import ct.buildcraft.lib.expression.node.condition.NodeConditionalBoolean;
import ct.buildcraft.lib.expression.node.condition.NodeConditionalDouble;
import ct.buildcraft.lib.expression.node.condition.NodeConditionalLong;
import ct.buildcraft.lib.expression.node.condition.NodeConditionalObject;
import ct.buildcraft.lib.expression.node.func.NodeFuncGenericToBoolean;
import ct.buildcraft.lib.expression.node.func.NodeFuncGenericToDouble;
import ct.buildcraft.lib.expression.node.func.NodeFuncGenericToLong;
import ct.buildcraft.lib.expression.node.func.NodeFuncGenericToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncObjectObjectToBoolean;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;
import ct.buildcraft.lib.expression.node.value.NodeConstantDouble;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;

public class InternalCompiler {
    private static final String UNARY_NEGATION = "¬";
    private static final String FUNCTION_START = "@";
    private static final String FUNCTION_ARGS = "#";
    private static final String OPERATORS = "+-*/^%~:?& << >> >>> == <= >= && || !=¬";
    private static final String leftAssociative = "+-^*/%||&&==!=<=>=<<>>?&";
    private static final String rightAssociative = "";
    private static final String[] precedence =
        { "(),", "?", "&& ||", "!= == <= >=", "<< >>", "+-", "%", "*/", "^", "~¬" };
    private static final String OPERATORS_SINGLE = "!~¬";

    private static final String LONG_REGEX = "[-+]?(0x[0-9a-fA-F_]+|[0-9]+)";
    private static final String DOUBLE_REGEX = "[-+]?[0-9]+(\\.[0-9]+)?";
    private static final String BOOLEAN_REGEX = "true|false";
    private static final String STRING_REGEX = "'.*'";

    private static final Pattern LONG_MATCHER = Pattern.compile(LONG_REGEX);
    private static final Pattern DOUBLE_MATCHER = Pattern.compile(DOUBLE_REGEX);
    private static final Pattern BOOLEAN_MATCHER = Pattern.compile(BOOLEAN_REGEX);
    private static final Pattern STRING_MATCHER = Pattern.compile(STRING_REGEX);

    public static IExpressionNode compileExpression(String expression, FunctionContext context)
        throws InvalidExpressionException {
        if (context == null) {
            context = new FunctionContext("default");
        }
        try {
            ExpressionDebugManager.debugPrintln("Compiling " + expression);
            String[] infix = tokenize(expression, context);
            String[] postfix = convertToPostfix(infix);
            ExpressionDebugManager.debugPrintln(Arrays.toString(postfix));
            return makeExpression(postfix, context);
        } catch (InvalidExpressionException iee) {
            throw new InvalidExpressionException("Failed to compile expression " + expression, iee);
        }
    }

    public static INodeFunc compileFunction(String expression, FunctionContext context, Argument... args)
        throws InvalidExpressionException {
        FunctionContext ctxReal = new FunctionContext(context);

        IVariableNode[] nodes = new IVariableNode[args.length];
        Class<?>[] types = new Class[args.length];
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
        } else if (node instanceof INodeObject<?>) {
            return new NodeFuncGenericToObject<>((INodeObject<?>) node, types, nodes);
        } else {
            ExpressionDebugManager.debugNodeClass(node.getClass());
            throw new IllegalStateException("Unknown node " + node.getClass());
        }
    }

    private static String[] tokenize(String function, FunctionContext context) throws InvalidExpressionException {
        String[] tokens = TokenizerDefaults.createTokenizer().tokenize(function);
        List<String> actual = new ArrayList<>();
        boolean changed = false;
        ExpressionDebugManager.debugPrintln("Incoming = " + Arrays.toString(tokens));

        for (int i = 0; i < tokens.length; i++) {
            String before = "";
            String token = tokens[i];
            int start = i;
            while (true) {
                token = tokens[i];
                ExpressionDebugManager.debugPrintln("  + " + token);
                ITokenizingContext ctx = ITokenizingContext.createFromString(token);
                TokenResult result = TokenizerDefaults.GOBBLER_WORD.tokenizePart(ctx);
                if (!(result instanceof ResultConsume)) {
                    i = start;
                    token = tokens[start];
                    ExpressionDebugManager.debugPrintln("  - not a word!");
                    break;
                }
                if (((ResultConsume) result).length != token.length()) {
                    i = start;
                    token = tokens[start];
                    ExpressionDebugManager.debugPrintln("  - different length!");
                    break;
                }
                String whole = before + token;
                String lookup = whole;
                if (whole.startsWith(".")) {
                    lookup = whole.substring(1);
                }
                int index = lookup.indexOf('.');
                if (index != -1) {
                    String type = lookup.substring(0, index);
                    String after = lookup.substring(index + 1);
                    FunctionContext ctx2 = NodeTypes.getContext(NodeTypes.getType(type));
                    if (ctx2 != null) {
                        if (ctx2.getVariable(after) != null || !ctx2.getFunctions(after).isEmpty()) {
                            token = whole;
                            break;
                        }
                    }
                }
                // Its a word -- but it might not be a valid one
                if (context.getVariable(lookup) != null || !context.getFunctions(lookup).isEmpty()) {
                    token = whole;
                    break;
                }
                // This word wasn't valid -- try the next one?
                before += token;
                i++;
                changed = true;
                if (i >= tokens.length) {
                    token = before;
                    ExpressionDebugManager.debugPrintln("  - too long!");
                    break;
                }
            }
            ExpressionDebugManager.debugPrintln("  -> " + token);
            actual.add(token);
        }
        return changed ? actual.toArray(new String[0]) : tokens;

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

                    boolean shouldContinue = leftAssociative.contains(token)
                        && (continueIfEqual ? tokenPrec <= stackPrec : tokenPrec < stackPrec);
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

    private static IExpressionNode makeExpression(String[] postfix, FunctionContext context)
        throws InvalidExpressionException {
        NodeStack stack = new NodeStack();
        for (int i = 0; i < postfix.length; i++) {
            String op = postfix[i];
            if (OPERATORS.contains(op) && !"?".equals(op) && !":".equals(op)) {
                boolean isNegation = UNARY_NEGATION.equals(op);
                int count = 2;
                if (isNegation || OPERATORS_SINGLE.contains(op)) {
                    op = isNegation ? "-" : op;
                    count = 1;
                }
                String function = op + FUNCTION_ARGS + count;
                pushFunctionNode(stack, function, context);
            } else if (":".equals(op)) continue; // NO-OP, all handled by "?"
            else if ("?".equals(op)) pushConditional(stack);
            else if (isValidLong(op)) {
                long val = parseValidLong(op);
                stack.push(new NodeConstantLong(val));
            } else if (isValidDouble(op)) {
                stack.push(new NodeConstantDouble(Double.parseDouble(op)));
            } else if (BOOLEAN_MATCHER.matcher(op).matches()) {
                stack.push(NodeConstantBoolean.of(Boolean.parseBoolean(op)));
            } else if (STRING_MATCHER.matcher(op).matches()) {
                stack.push(new NodeConstantObject<>(String.class, op.substring(1, op.length() - 1)));
            } else if (op.startsWith(FUNCTION_START)) {
                // Its a function
                String function = op.substring(1);
                pushFunctionNode(stack, function, context);
            } else {
                IExpressionNode node = context == null ? null : context.getVariable(op);
                if (node == null && op.contains(".")) {
                    int index = op.indexOf('.');
                    String type = op.substring(0, index);
                    FunctionContext ctx = getContext(type);
                    if (ctx != null) {
                        node = ctx.getVariable(op);
                        if (node == null) {
                            node = ctx.getVariable(op.substring(index + 1));
                        }
                    }
                }
                if (node != null) {
                    stack.push(node);
                } else {
                    String vars = getValidVariablesErrorString(context);
                    throw new InvalidExpressionException("Unknown variable '" + op + "'" + vars);
                }
            }
        }

        IExpressionNode node = stack.pop().inline();
        if (!stack.isEmpty()) {
            throw new InvalidExpressionException("Tried to make an expression with too many nodes! (" + stack + ")");
        }
        return node;
    }

    private static String getValidVariablesErrorString(FunctionContext context) {
        if (context == null) {
            return " (No context to get variables from)";
        }
        String vars = "\nList of valid variables:";
        vars += addParentVariables(context);
        return vars + "\n";
    }

    private static String addParentVariables(FunctionContext context) {
        String vars = "";
        List<String> allVariables = new ArrayList<>();
        allVariables.addAll(context.getAllVariables());
        allVariables.sort(Comparator.naturalOrder());
        if (!allVariables.isEmpty()) {
            if (!context.name.isEmpty()) {
                vars += "\n" + context.name + ":";
            }
            vars += "\n  " + allVariables.toString().replace("[", "").replace("]", "");
        }
        for (FunctionContext parent : context.getParents()) {
            vars += addParentVariables(parent);
        }
        return vars;
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

    public static IExpressionNode convertBinary(IExpressionNode convert, IExpressionNode compare)
        throws InvalidExpressionException {
        Class<?> convertClass = NodeTypes.getType(convert);
        Class<?> compareClass = NodeTypes.getType(compare);
        if (convertClass == compareClass) {
            return convert;
        }
        try {
            return NodeCasting.castToType(convert, compareClass);
        } catch (InvalidExpressionException iee) {
            try {
                NodeCasting.castToType(compare, convertClass);
                return convert;
            } catch (InvalidExpressionException iee2) {
                throw iee2;
            }
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
            } else if (right instanceof INodeObject) {
                stack.push(new NodeConditionalObject(condition, (INodeObject) left, (INodeObject) right));
            } else if (right instanceof INodeLong) {
                stack.push(new NodeConditionalLong(condition, (INodeLong) left, (INodeLong) right));
            } else {
                throw new InvalidExpressionException("Unknown node " + left);
            }

        } else {
            throw new InvalidExpressionException(
                "Required a boolean node, but got '" + conditional + "' of " + conditional.getClass());
        }
    }

    private static void pushFunctionNode(NodeStack stack, String function, FunctionContext context)
        throws InvalidExpressionException {
        String name = function.substring(0, function.indexOf(FUNCTION_ARGS));
        String argCount = function.substring(function.indexOf(FUNCTION_ARGS) + 1);
        int count = Integer.parseInt(argCount);

        if (name.startsWith(".")) {
            /*
             * Allow object style function calling by making the called node be the first argument to the function,
             * pushing all other nodes back
             */
            name = name.substring(1);
            count++;
        }

        List<IExpressionNode> popOrder = stack.peek(count);
        List<Class<?>> functionOrder = new ArrayList<>(popOrder.size());
        Map<List<Class<?>>, INodeFunc> functions = new HashMap<>();
        if (name.contains(".")) {
            int index = name.indexOf('.');
            String type = name.substring(0, index);
            FunctionContext ctx = getContext(type);
            if (ctx != null) {
                functions.putAll(ctx.getFunctions(name.substring(index + 1)));
            }
        }
        FunctionContext[] ctxs = new FunctionContext[count + 1];
        ctxs[0] = context;
        int n = 1;
        for (IExpressionNode node : popOrder) {
            Class<?> nodeType = NodeTypes.getType(node);
            functionOrder.add(0, nodeType);
            ctxs[n++] = NodeTypes.getContext(nodeType);
        }
        FunctionContext typeAwareContext = new FunctionContext(ctxs);
        functions.putAll(typeAwareContext.getFunctions(name));

        INodeFunc bestFunction = null;
        int bestCastCount = Integer.MAX_VALUE;
        List<INodeFunc> bestCasters = null;
        List<Class<?>> bestClassesTo = null;
        List<String> fnOrderNames = functionOrder.stream().map(NodeTypes::getName).collect(Collectors.toList());
        ExpressionDebugManager.debugStart("Finding best function called '" + name + "' for " + fnOrderNames);
        for (Map.Entry<List<Class<?>>, INodeFunc> func : functions.entrySet()) {
            List<Class<?>> functionClasses = func.getKey();
            List<String> fnClassNames = functionClasses.stream().map(NodeTypes::getName).collect(Collectors.toList());
            ExpressionDebugManager.debugPrintln("Found " + fnClassNames);
            if (functionClasses.size() != functionOrder.size()) {
                continue;
            }
            int casts = 0;
            boolean canCast = true;
            List<INodeFunc> casters = new ArrayList<>();
            ExpressionDebugManager.debugStart("Finding casters...");
            for (int i = 0; i < functionClasses.size(); i++) {
                Class<?> from = functionOrder.get(i);
                Class<?> to = functionClasses.get(i);
                ExpressionDebugManager.debugPrintln("  - " + NodeTypes.getName(from) + " -> " + NodeTypes.getName(to));
                if (from == to) {
                    casters.add(a -> a.pop(from));
                    ExpressionDebugManager.debugPrintln("    - Equal types, no cast needed.");
                    continue;
                }
                INodeFunc caster;
                if (from == long.class && to == INodeLong.class) {
                    caster = new NodeFuncWrapper() {
                        @Override
                        public IExpressionNode getNode(INodeStack s) throws InvalidExpressionException {
                            return new NodeConstantObject<>(INodeLong.class, s.popLong());
                        }
                    };
                } else if (from == double.class && to == INodeDouble.class) {
                    caster = new NodeFuncWrapper() {
                        @Override
                        public IExpressionNode getNode(INodeStack s) throws InvalidExpressionException {
                            return new NodeConstantObject<>(INodeDouble.class, s.popDouble());
                        }
                    };
                } else if (from == long.class && to == INodeDouble.class) {
                    caster = new NodeFuncWrapper() {
                        @Override
                        public IExpressionNode getNode(INodeStack s) throws InvalidExpressionException {
                            INodeLong node = s.popLong();
                            INodeDouble nodeD = new NodeCastLongToDouble(node);
                            return new NodeConstantObject<>(INodeDouble.class, nodeD.inline());
                        }
                    };
                } else if (from == boolean.class && to == INodeBoolean.class) {
                    caster = new NodeFuncWrapper() {
                        @Override
                        public IExpressionNode getNode(INodeStack s) throws InvalidExpressionException {
                            return new NodeConstantObject<>(INodeBoolean.class, s.popBoolean());
                        }
                    };
                } else {
                    FunctionContext castingCtx =
                        new FunctionContext(NodeTypes.getContext(from), NodeTypes.getContext(to));
                    caster = castingCtx.getFunction("(" + NodeTypes.getName(to) + ")", Collections.singletonList(from));
                    if (caster == null) {
                        ExpressionDebugManager.debugPrintln("    - No cast found!");
                        canCast = false;
                        break;
                    }
                }
                casts++;
                casters.add(caster);
                ExpressionDebugManager.debugPrintln("    - Caster = " + caster);
            }
            ExpressionDebugManager.debugEnd("");
            if (!canCast) {
                continue;
            }
            if (casts < bestCastCount) {
                bestCastCount = casts;
                bestFunction = func.getValue();
                bestCasters = casters;
                bestClassesTo = functionClasses;
            }
        }
        ExpressionDebugManager.debugEnd("Best = " + bestFunction);

        if (bestFunction == null || bestCasters == null) {
            // Allow any object to be compared to itself with == and !=
            boolean isEq = "==".equals(name);
            boolean isNE = "!=".equals(name);
            if (count == 2 && (isEq | isNE) && functionOrder.get(0) == functionOrder.get(1)) {
                Class<?> cls = functionOrder.get(0);
                NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<?, ?> func =
                    isEq ? Objects::equals : (a, b) -> !Objects.equals(a, b);
                bestFunction = new NodeFuncObjectObjectToBoolean(name, cls, cls, func);
                bestCastCount = 0;
                bestCasters = Collections.emptyList();
                bestClassesTo = new ArrayList<>(functionOrder);
                ExpressionDebugManager.debugPrintln("Using implicit object equality comparison.");
            } else {
                throw new InvalidExpressionException("No viable function called '" + name + "' found for "
                    + fnOrderNames + getValidFunctionsErrorString(typeAwareContext));
            }
        }

        NodeStack realStack;
        if (bestCastCount == 0) {
            realStack = stack;
        } else {
            IExpressionNode[] nodes = new IExpressionNode[count];
            for (int i = count - 1; i >= 0; i--) {
                INodeFunc caster = bestCasters.get(i);
                Class<?> from = functionOrder.get(i);
                Class<?> to = bestClassesTo.get(i);
                stack.setRecorder(Collections.singletonList(from), caster);
                IExpressionNode node = caster.getNode(stack);
                stack.checkAndRemoveRecorder();
                Class<?> actual = NodeTypes.getType(node);
                if (actual != to) {
                    throw new IllegalStateException("The caster " + caster
                        + " didn't produce the correct result! (Expected " + to + ", but got " + actual + ")");
                }
                nodes[i] = node;
            }
            ExpressionDebugManager.debugPrintln("Nodes = " + nodes);
            realStack = new NodeStack(nodes);
        }
        INodeFunc func = bestFunction;

        bestClassesTo = new ArrayList<>(bestClassesTo);
        Collections.reverse(bestClassesTo);
        realStack.setRecorder(bestClassesTo, func);
        IExpressionNode node = func.getNode(realStack);
        realStack.checkAndRemoveRecorder();

        stack.push(node);
    }

    private static String getValidFunctionsErrorString(FunctionContext context) {
        if (context == null) {
            return " (No context to get functions from)";
        }
        String vars = "\nList of valid functions:";
        vars += addParentFunctions(context);
        return vars + "\n";
    }

    private static String addParentFunctions(FunctionContext context) {
        String vars = "";
        List<String> allFunctions = new ArrayList<>();
        allFunctions.addAll(context.getAllFunctions().keySet());
        allFunctions.sort(Comparator.naturalOrder());
        if (!allFunctions.isEmpty()) {
            if (!context.name.isEmpty()) {
                vars += "\n" + context.name + ":";
            }
            for (String fnName : allFunctions) {
                Map<List<Class<?>>, INodeFunc> functions = context.getFunctions(fnName);
                for (Map.Entry<List<Class<?>>, INodeFunc> entry : functions.entrySet()) {
                    String args = "";
                    for (Class<?> arg : entry.getKey()) {
                        if (args.length() > 0) {
                            args += ", ";
                        }
                        args += NodeTypes.getName(arg);
                    }
                    INodeFunc function = entry.getValue();
                    String ret;
                    if (function instanceof INodeFuncBoolean) {
                        ret = NodeTypes.getName(boolean.class);
                    } else if (function instanceof INodeFuncDouble) {
                        ret = NodeTypes.getName(double.class);
                    } else if (function instanceof INodeFuncLong) {
                        ret = NodeTypes.getName(long.class);
                    } else {
                        ret = NodeTypes.getName(((INodeFuncObject<?>) function).getType());
                    }
                    vars += "\n  " + fnName + "(" + args + ") -> " + ret;
                }
            }
        }
        for (FunctionContext parent : context.getParents()) {
            vars += addParentFunctions(parent);
        }
        return vars;
    }

    private static FunctionContext getContext(String type) throws InvalidExpressionException {
        Class<?> clazz = NodeTypes.getType(type);
        return NodeTypes.getType(clazz);
    }

    /** Provided for wrapping types -- such as long -> INodeLong (Specifically INodeLong ->
     * INodeObject{@code <INodeLong>}) */
    private static abstract class NodeFuncWrapper implements INodeFunc {
        @Override
        public String toString() {
            return "[Internal Type Wrapper]";
        }
    }
}
