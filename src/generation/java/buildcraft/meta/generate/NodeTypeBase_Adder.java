package buildcraft.meta.generate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeTypeBase_Adder extends AutoGenerateFile {

    private static final String PATH = "buildcraft/lib/expression/NodeTypeBase.java";

    public NodeTypeBase_Adder() {
        super(PATH + ".txt");
    }

    @Override
    protected void callFileGen() {
        String replace = "";
        String imports = "";

        for (Map<String, String> types : FunctionContextBase_Adder.allFunctionTypes) {
            String args = types.get("a");
            if (!args.startsWith("o")) {
                continue;
            }
            String ret = types.get("r");

            String original = ret + "_t" + args.substring(1);

            int objectCount = (int) original.chars().filter(i -> i == 'o').count();
            int[] indexObject = new int[objectCount];
            int j = 0;
            for (int i = 0; i < original.length(); i++) {
                if (original.charAt(i) == 'o') {
                    indexObject[j++] = i;
                }
            }
            String importStart = "import buildcraft.lib.expression.node.func.NodeFunc";
            imports += importStart + replaceAll("{$Args}To{$ReturnOnly}.IFunc{$Args}To{$ReturnOnly};\n", types);
            System.out.println(ret + "_" + args + ":");
            for (int i = 0; i <= objectCount; i++) {
                Map<String, String> map = new LinkedHashMap<>(types);
                String repl = original;
                String[] classArgs = types.get("ObjectClassArgs").split(", ");
                String[] classArgsPass = types.get("ObjectClassArgsPass").split(", ");
                String string = types.get("TypeArgs");
                String[] typeArgs = string.substring(1, string.length() - 1).split(", ");
                String[] typeArgsPass = Arrays.copyOf(typeArgs, typeArgs.length);
                classArgs[0] = "";
                classArgsPass[0] = "getType()";
                typeArgs[0] = "";
                typeArgsPass[0] = "T";
                for (int k = 1; k <= i; k++) {
                    int index = indexObject[k - 1];
                    int arrIndex = k == 1 ? classArgs.length - 1 : k - 1;
                    classArgs[arrIndex] = "";
                    classArgsPass[arrIndex] = "getType()";
                    typeArgs[arrIndex] = "";
                    typeArgsPass[arrIndex] = "T";
                    repl = repl.substring(0, index) + 't' + repl.substring(index + 1);
                }

                map.put("a2", repl.substring(2));
                map.put("r2", repl.substring(0, 1));
                map.put("ObjectClassArgs2", replaceAll(join(classArgs, ", "), ", , ", ", "));
                map.put("ObjectClassArgsPass2", join(classArgsPass, ", "));
                String repl2 = replaceAll(join2(typeArgs, ", "), ", , ", ", ");
                if (repl2.startsWith(", ")) {
                    repl2 = repl2.substring(2);
                }
                if (repl2.endsWith(", ")) {
                    repl2 = repl2.substring(0, repl2.length() - 2);
                }
                if (repl2.length() > 0) {
                    repl2 = "<" + repl2 + ">";
                }
                map.put("TypeArgs2", repl2);
                map.put("TypeArgsPass2", "<" + replaceAll(join2(typeArgsPass, ", "), ", , ", ", ") + ">");
                if (repl.startsWith("t")) {
                    map.put("Return", "Object<T>");
                }

                System.out.println(" - " + repl + ", TypeArgs2 = " + map.get("TypeArgs2") + ", TypeArgsPass2 = "
                    + map.get("TypeArgsPass2"));

                String method = "\n\tpublic {$TypeArgs2} INodeFunc{$Return} put_{$a2}_{$r2}("
                    + "String name{$ObjectClassArgs2}IFunc{$Args}To{$ReturnOnly}{$TypeArgsPass2} func) {";
                method += "\n\t\treturn put_{$a}_{$r}(name, {$ObjectClassArgsPass2}func);";
                method.replace(" ", " ");
                method += "\n\t}\n";
                method = replaceAll(method, map);

                replace += method;
            }
        }

        Map<String, String> map = new HashMap<>();
        map.put("PutMethods", replace);
        map.put("Imports", imports);
        generateFile(PATH, map);
    }

    private static String join(String[] arr, String seperator) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            if (i != 0) {
                s += seperator;
            }
            s += arr[i];
        }
        s += seperator;
        return s;
    }

    private static String join2(String[] arr, String seperator) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            if (i != 0) {
                s += seperator;
            }
            s += arr[i];
        }
        return s;
    }

    private static String replaceAll(String in, String test, String with) {
        while (in.contains(test)) {
            in = in.replace(test, with);
        }
        return in;
    }
}
