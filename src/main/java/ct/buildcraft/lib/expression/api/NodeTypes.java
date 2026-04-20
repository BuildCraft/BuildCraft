package ct.buildcraft.lib.expression.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import ct.buildcraft.lib.expression.FunctionContext;
import ct.buildcraft.lib.expression.VecDouble;
import ct.buildcraft.lib.expression.VecLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncBoolean;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncDouble;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncLong;
import ct.buildcraft.lib.expression.api.INodeFunc.INodeFuncObject;
import ct.buildcraft.lib.expression.node.cast.NodeCasting;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncDoubleToObject;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToBoolean;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToDouble;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToLong;
import ct.buildcraft.lib.expression.node.func.gen.NodeFuncLongToObject;
import ct.buildcraft.lib.expression.node.value.NodeConstantBoolean;
import ct.buildcraft.lib.expression.node.value.NodeConstantDouble;
import ct.buildcraft.lib.expression.node.value.NodeConstantLong;
import ct.buildcraft.lib.expression.node.value.NodeConstantObject;
import ct.buildcraft.lib.expression.node.value.NodeVariable;
import ct.buildcraft.lib.expression.node.value.NodeVariableBoolean;
import ct.buildcraft.lib.expression.node.value.NodeVariableDouble;
import ct.buildcraft.lib.expression.node.value.NodeVariableLong;
import ct.buildcraft.lib.expression.node.value.NodeVariableObject;

public class NodeTypes {

    public static final FunctionContext LONG = LongFunctions.LONG;
    public static final FunctionContext DOUBLE = DoubleFunctions.DOUBLE;
    public static final FunctionContext BOOLEAN;
    public static final NodeType<String> STRING;
    public static final NodeType<VecLong> VEC_LONG;
    public static final NodeType<VecDouble> VEC_DOUBLE;
    public static final NodeType<INodeLong> NODE_LONG;
    public static final NodeType<INodeDouble> NODE_DOUBLE;
    public static final NodeType<INodeBoolean> NODE_BOOLEAN;

    private static final Map<String, Class<?>> typesByName = new HashMap<>();
    private static final Map<Class<?>, String> namesByType = new HashMap<>();

    /** All of the OBJECT types. Unlike {@link #typesByName} that this doesn't include long, double, or boolean */
    public static final Map<Class<?>, NodeType<?>> typesByClass = new HashMap<>();

    static {
        BOOLEAN = new FunctionContext("Type: Boolean");
        STRING = new NodeType<>("String", "");
        VEC_LONG = new NodeType<>("Long Vector", VecLong.ZERO);
        VEC_DOUBLE = new NodeType<>("Double Vector", VecDouble.ZERO);
        NODE_LONG = new NodeType<>("Long Node", INodeLong.class, NodeConstantLong.ZERO);
        NODE_DOUBLE = new NodeType<>("Double Node", INodeDouble.class, NodeConstantDouble.ZERO);
        NODE_BOOLEAN = new NodeType<>("Boolean Node", INodeBoolean.class, NodeConstantBoolean.FALSE);

        typesByName.put("long", long.class);
        typesByName.put("int", long.class);
        typesByName.put("double", double.class);
        typesByName.put("float", double.class);
        typesByName.put("boolean", boolean.class);
        typesByName.put("bool", boolean.class);
        namesByType.put(long.class, "long");
        namesByType.put(double.class, "double");
        namesByType.put(boolean.class, "boolean");
        addType("String", STRING);
        addType("VecLong", VEC_LONG);
        addType("VecDouble", VEC_DOUBLE);
        addType("NodeLong", NODE_LONG);
        addType("NodeDouble", NODE_DOUBLE);
        addType("NodeBoolean", NODE_BOOLEAN);

        BOOLEAN.put_b_b("!", (a) -> !a, a -> "!" + a);
        BOOLEAN.put_bb_b("^", (a, b) -> a ^ b, (a, b) -> "(" + a + "^" + b + ")");
        BOOLEAN.put_bb_b("&", (a, b) -> a & b, (a, b) -> "(" + a + "&" + b + ")");
        BOOLEAN.put_bb_b("|", (a, b) -> a | b, (a, b) -> "(" + a + "|" + b + ")");
        BOOLEAN.put_bb_b("&&", (a, b) -> a && b, (a, b) -> "(" + a + "&&" + b + ")");
        BOOLEAN.put_bb_b("||", (a, b) -> a || b, (a, b) -> "(" + a + "||" + b + ")");
        BOOLEAN.put_bb_b("==", (a, b) -> a == b, (a, b) -> "(" + a + "==" + b + ")");
        BOOLEAN.put_bb_b("!=", (a, b) -> a != b, (a, b) -> "(" + a + "!=" + b + ")");
        BOOLEAN.put_b_o("(string)", String.class, a -> "" + a, (a) -> "((string) " + a + ")");

        STRING.put_tt_t("+", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
        STRING.put_tt_t("&", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
        STRING.put_tt_b("<", (a, b) -> a.compareTo(b) < 0, (a, b) -> "(" + a + " < " + b + ")");
        STRING.put_tt_b(">", (a, b) -> a.compareTo(b) > 0, (a, b) -> "(" + a + " > " + b + ")");
        STRING.put_tt_b("==", (a, b) -> Objects.equals(a, b), (a, b) -> "(" + a + " == " + b + ")");
        STRING.put_tt_b("!=", (a, b) -> !Objects.equals(a, b), (a, b) -> "(" + a + " != " + b + ")");
        STRING.put_tt_b("<=", (a, b) -> a.compareTo(b) <= 0, (a, b) -> "(" + a + " <= " + b + ")");
        STRING.put_tt_b(">=", (a, b) -> a.compareTo(b) >= 0, (a, b) -> "(" + a + " >= " + b + ")");

        STRING.put_t_l("length", String::length, a -> a + ".length()");
        STRING.put_t_t("toLowerCase", a -> a.toLowerCase(Locale.ROOT), a -> a + ".toLowerCase()");
        STRING.put_t_t("toUpperCase", a -> a.toUpperCase(Locale.ROOT), a -> a + ".toUpperCase()");
        STRING.put_tl_t("char_at", NodeTypes::functionCharAt);
        STRING.put_tll_t("substring", NodeTypes::functionSubstring);
        STRING.put_tll_t("substring_rel", NodeTypes::functionSubstringRelative);

        VEC_LONG.putConstant("ZERO", VecLong.ZERO);
        VEC_LONG.put_l_t("vec", VecLong::new);
        VEC_LONG.put_ll_t("vec", VecLong::new);
        VEC_LONG.put_lll_t("vec", VecLong::new);
        VEC_LONG.put_llll_t("vec", VecLong::new);
        VEC_LONG.put_t_o("(VecDouble)", VecDouble.class, VecLong::castToDouble);

        VEC_LONG.put_tt_t("+", VecLong::add);
        VEC_LONG.put_tt_t("-", VecLong::sub);
        VEC_LONG.put_tt_t("*", VecLong::scale);
        VEC_LONG.put_tt_t("/", VecLong::div);
        VEC_LONG.put_tt_t("cross", VecLong::crossProduct);
        VEC_LONG.put_tt_d("distanceTo", VecLong::distance);
        VEC_LONG.put_tt_l("dot2", VecLong::dotProduct2);
        VEC_LONG.put_tt_l("dot3", VecLong::dotProduct3);
        VEC_LONG.put_tt_l("dot4", VecLong::dotProduct4);
        VEC_LONG.put_t_d("length", VecLong::length);
        VEC_LONG.put_t_o("(string)", String.class, VecLong::toString);

        VEC_DOUBLE.put_d_t("vec", VecDouble::new);
        VEC_DOUBLE.put_dd_t("vec", VecDouble::new);
        VEC_DOUBLE.put_ddd_t("vec", VecDouble::new);
        VEC_DOUBLE.put_dddd_t("vec", VecDouble::new);

        VEC_DOUBLE.put_tt_t("+", VecDouble::add);
        VEC_DOUBLE.put_tt_t("-", VecDouble::sub);
        VEC_DOUBLE.put_tt_t("*", VecDouble::scale);
        VEC_DOUBLE.put_tt_t("/", VecDouble::div);
        VEC_DOUBLE.put_tt_t("cross", VecDouble::crossProduct);
        VEC_DOUBLE.put_tt_d("distanceTo", VecDouble::distance);
        VEC_DOUBLE.put_tt_d("dot2", VecDouble::dotProduct2);
        VEC_DOUBLE.put_tt_d("dot3", VecDouble::dotProduct3);
        VEC_DOUBLE.put_tt_d("dot4", VecDouble::dotProduct4);
        VEC_DOUBLE.put_t_d("length", VecDouble::length);
        VEC_DOUBLE.put_t_o("(string)", String.class, VecDouble::toString);
    }

    private static String functionCharAt(String str, long index) {
        return functionSubstringRelative(str, index, 1);
    }

    private static String functionSubstring(String str, long indexStart, long indexEnd) {
        if (indexStart >= indexEnd) {
            return "";
        }
        if (indexStart < 0) {
            return "";
        }
        if (indexEnd >= str.length()) {
            return "";
        }
        return str.substring((int) indexStart, (int) indexEnd);
    }

    private static String functionSubstringRelative(String str, long indexStart, long length) {
        return functionSubstring(str, indexStart, indexStart + length);
    }

    public static Class<?> getType(String name) {
        return typesByName.get(name.toLowerCase(Locale.ROOT));
    }

    public static Class<?> parseType(String type) throws InvalidExpressionException {
        Class<?> clazz = getType(type);
        if (clazz != null) {
            return clazz;
        }
        throw new InvalidExpressionException("Unknown type " + type + ", must be one of " + typesByName.keySet());
    }

    public static <T> NodeType<T> getType(Class<T> clazz) {
        return (NodeType<T>) typesByClass.get(clazz);
    }

    public static String getName(Class<?> clazz) {
        return namesByType.get(clazz);
    }

    public static Collection<String> getValidTypeNames() {
        return typesByName.keySet();
    }

    public static FunctionContext getContext(Class<?> clazz) {
        if (clazz == long.class) return LONG;
        if (clazz == double.class) return DOUBLE;
        if (clazz == boolean.class) return BOOLEAN;
        return typesByClass.get(clazz);
    }

    public static <T> void addType(NodeType<T> type) {
        addType(type.name, type);
    }

    public static <T> void addType(String key, NodeType<T> type) {
        key = key.toLowerCase(Locale.ROOT);
        namesByType.put(type.type, key);
        typesByName.put(key, type.type);
        typesByClass.put(type.type, type);
    }

    public static Class<?> getType(IExpressionNode node) {
        if (node instanceof INodeObject<?>) {
            return ((INodeObject<?>) node).getType();
        } else if (node instanceof INodeLong) return long.class;
        else if (node instanceof INodeDouble) return double.class;
        else if (node instanceof INodeBoolean) return boolean.class;
        else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    public static Class<?> getType(INodeFunc node) {
        if (node instanceof INodeFuncObject<?>) {
            return ((INodeFuncObject<?>) node).getType();
        } else if (node instanceof INodeFuncLong) return long.class;
        else if (node instanceof INodeFuncDouble) return double.class;
        else if (node instanceof INodeFuncBoolean) return boolean.class;
        else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    public static NodeVariable makeVariableNode(Class<?> type, String name) {
        if (type == long.class) return new NodeVariableLong(name);
        if (type == double.class) return new NodeVariableDouble(name);
        if (type == boolean.class) return new NodeVariableBoolean(name);
        return new NodeVariableObject<>(name, type);
    }

    public static IConstantNode createConstantNode(IExpressionNode node) {
        if (node instanceof INodeLong) return new NodeConstantLong(((INodeLong) node).evaluate());
        else if (node instanceof INodeDouble) return new NodeConstantDouble(((INodeDouble) node).evaluate());
        else if (node instanceof INodeBoolean) return NodeConstantBoolean.of(((INodeBoolean) node).evaluate());
        else if (node instanceof INodeObject) {
            INodeObject<?> nodeObj = (INodeObject<?>) node;
            return createConstantObject(nodeObj);
        } else throw new IllegalArgumentException("Illegal node " + node.getClass());
    }

    private static <T> IConstantNode createConstantObject(INodeObject<T> nodeObj) {
        return new NodeConstantObject<>(nodeObj.getType(), nodeObj.evaluate());
    }

    public static IExpressionNode cast(IExpressionNode node, Class<?> to) throws InvalidExpressionException {
        if (to == double.class) return NodeCasting.castToDouble(node);
        if (to == String.class) return NodeCasting.castToString(node);
        if (to == long.class) {
            if (node instanceof INodeLong) {
                return node;
            } else {
                throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a long");
            }
        }
        if (to == boolean.class) {
            if (node instanceof INodeBoolean) {
                return node;
            } else {
                throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a boolean");
            }
        }
        throw new IllegalStateException("Unknown node type '" + to + "'");
    }

    public static class LongFunctions {

        public static final FunctionContext LONG;

        public static final NodeFuncLongToLong NEGATE;
        public static final NodeFuncLongToLong BITWISE_INVERT;
        public static final NodeFuncLongLongToLong ADD, SUB, MUL, DIV, MOD;
        public static final NodeFuncLongLongToLong BITWISE_XOR, BITWISE_AND, BITWISE_OR;

        /** Relational operators:
         * <ul>
         * <li>LT: "&lt;", less than</li>
         * <li>GT: "&gt;", greater than</li>
         * <li>LE: "&lt;=", less than or equals</li>
         * <li>GE: "&gt;=", greater than or equals</li>
         * <li>EQ: "==", equals</li>
         * <li>NE: "!=", not equal</li>
         * </ul>
         */
        public static NodeFuncLongLongToBoolean LT, GT, LE, GE, EQ, NE;
        public static NodeFuncLongLongToLong BITSHIFT_UP;
        public static NodeFuncLongLongToLong BITSHIFT_DOWN;
        public static NodeFuncLongLongToLong BITSHIFT_DOWN_HARD;

        public static NodeFuncLongToDouble CVT_DOUBLE;
        public static NodeFuncLongToObject<String> CVT_STRING;

        static {
            LONG = new FunctionContext("Type: Long");

            NEGATE = LONG.put_l_l("-", (a) -> -a, a -> "-(" + a + ")");
            BITWISE_INVERT = LONG.put_l_l("~", (a) -> ~a, a -> "~(" + a + ")");
            ADD = LONG.put_ll_l("+", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
            SUB = LONG.put_ll_l("-", (a, b) -> a - b, (a, b) -> "(" + a + " - " + b + ")");
            MUL = LONG.put_ll_l("*", (a, b) -> a * b, (a, b) -> "(" + a + " * " + b + ")");
            DIV = LONG.put_ll_l("/", (a, b) -> a / b, (a, b) -> "(" + a + " / " + b + ")");
            MOD = LONG.put_ll_l("%", (a, b) -> a % b, (a, b) -> "(" + a + " % " + b + ")");
            BITWISE_XOR = LONG.put_ll_l("^", (a, b) -> a ^ b, (a, b) -> "(" + a + " ^ " + b + ")");
            BITWISE_AND = LONG.put_ll_l("&", (a, b) -> a & b, (a, b) -> "(" + a + " & " + b + ")");
            BITWISE_OR = LONG.put_ll_l("|", (a, b) -> a | b, (a, b) -> "(" + a + " | " + b + ")");
            LT = LONG.put_ll_b("<", (a, b) -> a < b, (a, b) -> "(" + a + " < " + b + ")");
            GT = LONG.put_ll_b(">", (a, b) -> a > b, (a, b) -> "(" + a + " > " + b + ")");
            LE = LONG.put_ll_b("<=", (a, b) -> a <= b, (a, b) -> "(" + a + " <= " + b + ")");
            GE = LONG.put_ll_b(">=", (a, b) -> a >= b, (a, b) -> "(" + a + " >= " + b + ")");
            EQ = LONG.put_ll_b("==", (a, b) -> a == b, (a, b) -> "(" + a + " == " + b + ")");
            NE = LONG.put_ll_b("!=", (a, b) -> a != b, (a, b) -> "(" + a + " != " + b + ")");
            BITSHIFT_UP = LONG.put_ll_l("<<", (a, b) -> a << b, (a, b) -> "(" + a + " << " + b + ")");
            BITSHIFT_DOWN = LONG.put_ll_l(">>", (a, b) -> a >> b, (a, b) -> "(" + a + " >> " + b + ")");
            BITSHIFT_DOWN_HARD = LONG.put_ll_l(">>>", (a, b) -> a >>> b, (a, b) -> a + " >>> " + b);
            CVT_DOUBLE = LONG.put_l_d("(double)", a -> a, (a) -> "((double) " + a + ")");
            CVT_STRING = LONG.put_l_o("(string)", String.class, a -> "" + a, (a) -> "((string) " + a + ")");
        }
    }

    public static class DoubleFunctions {

        public static final FunctionContext DOUBLE;

        public static final NodeFuncDoubleToDouble NEGATE;
        public static final NodeFuncDoubleDoubleToDouble ADD, SUB, MUL, DIV, MOD;

        /** Relational operators:
         * <ul>
         * <li>LT: "&lt;", less than</li>
         * <li>GT: "&gt;", greater than</li>
         * <li>LE: "&lt;=", less than or equals</li>
         * <li>GE: "&gt;=", greater than or equals</li>
         * <li>EQ: "==", equals</li>
         * <li>NE: "!=", not equal</li>
         * </ul>
         */
        public static NodeFuncDoubleDoubleToBoolean LT, GT, LE, GE, EQ, NE;

        public static NodeFuncDoubleToObject<String> CVT_STRING;

        static {
            DOUBLE = new FunctionContext("Type: Double");

            NEGATE = DOUBLE.put_d_d("-", (a) -> -a, a -> "-(" + a + ")");
            ADD = DOUBLE.put_dd_d("+", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
            SUB = DOUBLE.put_dd_d("-", (a, b) -> a - b, (a, b) -> "(" + a + " - " + b + ")");
            MUL = DOUBLE.put_dd_d("*", (a, b) -> a * b, (a, b) -> "(" + a + " * " + b + ")");
            DIV = DOUBLE.put_dd_d("/", (a, b) -> a / b, (a, b) -> "(" + a + " / " + b + ")");
            MOD = DOUBLE.put_dd_d("%", (a, b) -> a % b, (a, b) -> "(" + a + " % " + b + ")");
            LT = DOUBLE.put_dd_b("<", (a, b) -> a < b, (a, b) -> "(" + a + " < " + b + ")");
            GT = DOUBLE.put_dd_b(">", (a, b) -> a > b, (a, b) -> "(" + a + " > " + b + ")");
            LE = DOUBLE.put_dd_b("<=", (a, b) -> a <= b, (a, b) -> "(" + a + " <= " + b + ")");
            GE = DOUBLE.put_dd_b(">=", (a, b) -> a >= b, (a, b) -> "(" + a + " >= " + b + ")");
            EQ = DOUBLE.put_dd_b("==", (a, b) -> a == b, (a, b) -> "(" + a + " == " + b + ")");
            NE = DOUBLE.put_dd_b("!=", (a, b) -> a != b, (a, b) -> "(" + a + " != " + b + ")");
            CVT_STRING = DOUBLE.put_d_o("(string)", String.class, a -> "" + a, (a) -> "((string) " + a + ")");
        }
    }
}
