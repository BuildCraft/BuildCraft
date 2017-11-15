package buildcraft.meta.generate;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class NodeFunc_A_To_B extends AutoGenerateFile {

    static final String[] ARG_COUNT = { "$$NOPE$$", "Bi", "Tri", "Quad", "Penta", "Hex" };

    public enum NodeTypeStr {
        LONG,
        DOUBLE,
        BOOLEAN,
        OBJECT;

        final String prop_return;
        final String prop_Capitalised;
        final String prop_StringArg = "{$index}";
        final String prop_StringArgL = "arg{$INDEX}";
        final String prop_ObjectClassArg;
        final String prop_ObjectClassField;
        final String prop_ObjectClassArgPass;
        final String prop_ObjectClassFieldSet;

        final String prop_ObjectNodeArg;
        final String prop_ObjectNodeField;
        final String prop_ObjectNodeFieldSet;

        final String prop_InstanceArg;
        final String prop_ToStringName;

        final String prop_ObjectTypeArg;
        final String prop_NodePop;
        final String prop_NodeConstant;

        // @formatter:off
        String _lowercase() { return prop_return; }
        String _Capitalised() { return prop_Capitalised; }
        String _StringArg() { return prop_StringArg; }
        String _StringArgL() { return prop_StringArgL; }
        String _ObjectClassArg() { return prop_ObjectClassArg; }
        String _ObjectClassField() { return prop_ObjectClassField; }
        String _ObjectClassArgPass() { return prop_ObjectClassArgPass; }
        String _ObjectClassFieldSet() { return prop_ObjectClassFieldSet; }
        String _ObjectNodeArg() { return prop_ObjectNodeArg; }
        String _ObjectNodeField() { return prop_ObjectNodeField; }
        String _ObjectNodeFieldSet() { return prop_ObjectNodeFieldSet; }
        String _ObjectTypeArg() { return prop_ObjectTypeArg; }
        String _NodePop() { return prop_NodePop; }
        String _NodeConstant() { return prop_NodeConstant; }
        String _InstanceArg() { return prop_InstanceArg; }
        String _ToStringName() { return prop_ToStringName; }
        // @formatter:on

        private NodeTypeStr() {
            if (name().startsWith("O")) {
                prop_return = "R";
                prop_Capitalised = "Object";

                prop_ObjectClassArg = "Class<{$INDEX}> argType{$INDEX}, ";
                prop_ObjectClassField = "\tprivate final Class<{$INDEX}> argType{$INDEX};\n";
                prop_ObjectClassArgPass = "argType{$INDEX}, ";
                prop_ObjectClassFieldSet = "\t\tthis.argType{$INDEX} = argType{$INDEX};\n";

                prop_ObjectNodeArg = "INodeObject<{$INDEX}> arg{$INDEX}";
                prop_ObjectNodeField = "\t\tprivate final INodeObject<{$INDEX}> arg{$INDEX};\n";
                prop_ObjectNodeFieldSet = "\t\t\tthis.arg{$INDEX} = arg{$INDEX};\n";
                prop_ObjectTypeArg = "{$INDEX}";
                prop_NodePop = "\t\tINodeObject<{$INDEX}> {$index} = stack.popObject(argType{$INDEX});";
                prop_NodeConstant = "new NodeConstantObject<>(returnType, ";
                prop_InstanceArg = "{$INDEX} {$index}";
                prop_ToStringName = "\" + NodeTypes.getName(argType{$INDEX}) + \"";
            } else {
                prop_return = name().toLowerCase(Locale.ROOT);
                prop_Capitalised = name().substring(0, 1) + prop_return.substring(1);
                prop_ObjectClassArg = "";
                prop_ObjectClassField = "";
                prop_ObjectClassArgPass = "";
                prop_ObjectClassFieldSet = "";
                prop_ObjectNodeArg = "INode" + prop_Capitalised + " arg{$INDEX}";
                prop_ObjectNodeField = "\t\tprivate final INode" + prop_Capitalised + " arg{$INDEX};\n";
                prop_ObjectNodeFieldSet = "\t\t\tthis.arg{$INDEX} = arg{$INDEX};\n";
                prop_ObjectTypeArg = "";
                prop_NodePop = "\t\tINode" + prop_Capitalised + " {$index} = stack.pop" + prop_Capitalised + "();";
                prop_NodeConstant = "NodeConstant" + prop_Capitalised + ".of(";
                prop_InstanceArg = prop_return + " {$index}";
                prop_ToStringName = prop_return;
            }
        }
    }

    private static final String PATH = "buildcraft/lib/expression/node/func/";

    public NodeFunc_A_To_B() {
        super(PATH + "NodeFunc{$Args}To{$Return}.java.txt");
    }

    @Override
    protected void callFileGen() {
        // Type Order: (EMPTY), L, D, B, O
        NodeTypeStr _long = NodeTypeStr.LONG;
        NodeTypeStr _double = NodeTypeStr.DOUBLE;
        NodeTypeStr _bool = NodeTypeStr.BOOLEAN;
        NodeTypeStr _obj = NodeTypeStr.OBJECT;

        generateType(_long, _long);
        generateType(_long, _long, _long);
        generateType(_long, _long, _long, _long);
        generateType(_long, _double);
        generateType(_long, _bool);
        generateType(_long, _obj);
        generateType(_long, _obj, _long);
        generateType(_long, _obj, _long, _long);
        generateType(_long, _obj, _obj);

        generateType(_double, _long);
        generateType(_double, _double);
        generateType(_double, _double, _double);
        generateType(_double, _double, _double, _double);
        generateType(_double, _obj);
        generateType(_double, _obj, _obj);

        generateType(_bool, _long);
        generateType(_bool, _long, _long);
        generateType(_bool, _double, _double);
        generateType(_bool, _bool);
        generateType(_bool, _bool, _bool);
        generateType(_bool, _obj);
        generateType(_bool, _obj, _obj);

        generateType(_obj, _long);
        generateType(_obj, _long, _long);
        generateType(_obj, _long, _long, _long);
        generateType(_obj, _long, _long, _long, _long);
        generateType(_obj, _double);
        generateType(_obj, _double, _double);
        generateType(_obj, _double, _double, _double);
        generateType(_obj, _double, _double, _double, _double);
        generateType(_obj, _bool);
        generateType(_obj, _obj);
        generateType(_obj, _obj, _long);
        generateType(_obj, _obj, _long, _long);
        generateType(_obj, _obj, _obj);
        generateType(_obj, _obj, _obj, _obj);
        generateType(_obj, _obj, _obj, _obj, _obj);
    }

    private void generateType(NodeTypeStr ret, NodeTypeStr... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No-arg ones don't need to be generated!");
        }

        Map<String, String> map = new LinkedHashMap<>();
        map.put("StringFunction", "StringFunction" + ARG_COUNT[args.length]);

        map.put("ObjectClassArgs", makeClassType(args, ret, NodeTypeStr::_ObjectClassArg));
        map.put("ObjectClassFields", makeClassType(args, ret, NodeTypeStr::_ObjectClassField));
        map.put("ObjectClassArgsPass", makeClassType(args, ret, NodeTypeStr::_ObjectClassArgPass));
        map.put("ObjectClassFieldSet", makeClassType(args, ret, NodeTypeStr::_ObjectClassFieldSet));

        map.put("ObjectNodeArgs", join1(args, NodeTypeStr::_ObjectNodeArg, ", "));
        map.put("ObjectNodeFields", join1(args, NodeTypeStr::_ObjectNodeField, ""));

        map.put("StringFunctionArgs", join1(args, NodeTypeStr::_StringArg, ", "));
        map.put("StringFunctionArgsL", join1(args, NodeTypeStr::_StringArgL, ", "));
        map.put("StringFunctionArgsPlus", join1(args, NodeTypeStr::_StringArg, " + \", \" + ") + " + ");

        map.put("ReturnOnly", ret.prop_Capitalised);
        boolean isObject = ret == NodeTypeStr.OBJECT;
        map.put("Return", ret.prop_Capitalised + (isObject ? "<R>" : ""));
        String typeArgs = "<" + join2(args, NodeTypeStr::_ObjectTypeArg, ", ");
        while (typeArgs.contains("<, ")) {
            typeArgs = typeArgs.replace("<, ", "<");
        }
        while (typeArgs.contains(", , ")) {
            typeArgs = typeArgs.replace(", , ", ", ");
        }
        if (isObject) {
            typeArgs += "R>";
        } else if (typeArgs.length() > 1) {
            typeArgs += ">";
            typeArgs = typeArgs.replace(", >", ">");
        }
        map.put("TypeArgs", typeArgs.length() > 2 ? typeArgs : "");

        if (isObject) {
            String getTypeFunc = "\n\t@Override\n\tpublic Class<R> getType() {\n\t\treturn returnType;\n\t}\n";
            map.put("ObjectGetType", getTypeFunc);
            map.put("ObjectGetType2", getTypeFunc.replace("\n\t", "\n\t\t"));
        } else {
            map.put("ObjectGetType", "");
            map.put("ObjectGetType2", "");
        }

        map.put("ObjectNodeFieldSet", join1(args, NodeTypeStr::_ObjectNodeFieldSet, ""));
        map.put("NodeStackPops", join1i(args, NodeTypeStr::_NodePop, "\n"));
        map.put("NodeConstantGetter", ret.prop_NodeConstant);
        map.put("NodeEvaluations", join1(args, t -> "{$index}.evaluate()", ", "));
        map.put("NodeEvaluationsL", join1(args, t -> "arg{$INDEX}.evaluate()", ", "));
        map.put("FunctionArgs", join1(args, NodeTypeStr::_InstanceArg, ", "));
        map.put("ToStringArgs", join1(args, t -> "\"{{$INDEX}}\"", ", "));
        map.put("NodeToStringArgs", join1(args, t -> t.prop_StringArgL + ".toString()", ", "));

        map.put("return", ret.prop_return);
        map.put("Args", join1(args, NodeTypeStr::_Capitalised, ""));
        map.put("a", join1(args, a -> a.name().toLowerCase(Locale.ROOT).substring(0, 1), ""));
        map.put("r", ret.name().toLowerCase(Locale.ROOT).substring(0, 1));
        String value = join1(args, NodeTypeStr::_ToStringName, ", ") + " -> ";
        map.put("ToStringName", value + ret._ToStringName().replace("argType{$INDEX}", "returnType"));

        FunctionContextBase_Adder.allFunctionTypes.add(map);
        generateFile(PATH + "NodeFunc{$Args}To{$ReturnOnly}.java", map);
    }

    private static String makeClassType(NodeTypeStr[] args, NodeTypeStr ret, Function<NodeTypeStr, String> getter) {
        return join1(args, getter, "") + getter.apply(ret)//
            .replace("{$INDEX}", "R")//
            .replace("arg", "return")//
            .replace("ypeR", "ype");
    }

    private static String join1(NodeTypeStr[] arr, Function<NodeTypeStr, String> getter, String seperator) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            if (i != 0) {
                s += seperator;
            }
            s += replaceForIndex(i, getter.apply(arr[i]));
        }
        return s;
    }

    private static String join2(NodeTypeStr[] arr, Function<NodeTypeStr, String> getter, String add) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            s += replaceForIndex(i, getter.apply(arr[i]));
            s += add;
        }
        return s;
    }

    private static String join1i(NodeTypeStr[] arr, Function<NodeTypeStr, String> getter, String add) {
        String s = "";
        for (int i = arr.length - 1; i >= 0; i--) {
            s += add;
            s += replaceForIndex(i, getter.apply(arr[i]));
        }
        s += add;
        return s;
    }

    private static String replaceForIndex(int i, String string) {
        String s = new String(new char[] { (char) ('a' + i) });
        return string//
            .replace("{$index}", s)//
            .replace("{$INDEX}", s.toUpperCase(Locale.ROOT));
    }
}
