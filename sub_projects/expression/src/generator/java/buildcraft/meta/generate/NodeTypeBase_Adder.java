package buildcraft.meta.generate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeTypeBase_Adder extends AutoGenerateFile {

    private static final String PATH = "/buildcraft/lib/expression/NodeTypeBase.java";

    public NodeTypeBase_Adder() {
        super(PATH + ".txt");
    }

    @Override
    protected void callFileGen() {
        String replace = "";
        String imports = "";

        for (Map<String, String> types : FunctionContextBase_Adder.allFunctionTypes) {
            String args = types.get("a");
            String ret = types.get("r");

            String original = ret + "_" + args;

            int objectCount = (int) original.chars().filter(i -> i == 'o').count();
            if (objectCount == 0) {
                continue;
            }
            int[] indexObject = new int[objectCount];
            int j = 0;
            for (int i = 0; i < original.length(); i++) {
                if (original.charAt(i) == 'o') {
                    indexObject[j++] = i;
                }
            }
            String importStart = "import buildcraft.lib.expression.node.func.NodeFunc";
            imports += importStart + replaceAll("{$Args}To{$ReturnOnly};\n", types);
            imports += importStart + replaceAll("{$Args}To{$ReturnOnly}.IFunc{$Args}To{$ReturnOnly};\n", types);
            if (AutoGenerator.verbose) {
                System.out.println(ret + "_" + args + ":");
            }
            if (objectCount > 2) {
                replace += "\n\t/////////////////////////";
                replace += "\n\t//";
                replace += "\n\t// put_" + args + "_" + ret + "";
                replace += "\n\t//";
                replace += "\n\t/////////////////////////";
                replace += "\n";
            } else {
                replace += "\n\t// put_" + args + "_" + ret + "\n";
            }
            for (int i = 1; i < (1 << objectCount); i++) {
                Map<String, String> map = new LinkedHashMap<>(types);
                String repl = original;
                String[] classArgs = types.get("ObjectClassArgs").split(", ");
                String[] classArgsPass = types.get("ObjectClassArgsPass").split(", ");
                String string = types.get("TypeArgs");
                String[] typeArgs = string.substring(1, string.length() - 1).split(", ");
                String[] typeArgsPass = Arrays.copyOf(typeArgs, typeArgs.length);
                for (int k = 0; k < objectCount; k++) {
                    if ((i & (1 << k)) != 0) {
                        int index = indexObject[k];
                        int arrIndex = k == 0 ? classArgs.length - 1 : k - 1;
                        classArgs[arrIndex] = "";
                        classArgsPass[arrIndex] = "getType()";
                        typeArgs[arrIndex] = "";
                        typeArgsPass[arrIndex] = "T";
                        repl = repl.substring(0, index) + 't' + repl.substring(index + 1);
                    }
                }

                map.put("a2", repl.substring(2));
                map.put("r2", repl.substring(0, 1));
                map.put("ObjectClassArgs2", replaceAll(", " + join(classArgs, ", "), ", , ", ", "));
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

                if (AutoGenerator.verbose) {
                    System.out.println(" - " + repl + ", TypeArgs2 = " + map.get("TypeArgs2") + ", TypeArgsPass2 = "
                        + map.get("TypeArgsPass2"));
                }

                String method =
                    "\n\tpublic {$TypeArgs2} NodeFunc{$Args}To{$ReturnOnly}{$TypeArgsPass2} put_{$a2}_{$r2}("
                        + "String fname{$ObjectClassArgs2}IFunc{$Args}To{$ReturnOnly}{$TypeArgsPass2} func) {";
                method += "\n\t\treturn put_{$a}_{$r}(fname, {$ObjectClassArgsPass2}func);";
                method.replace(" ", " ");
                method += "\n\t}\n";

                method += "\n\tpublic {$TypeArgs2} NodeFunc{$Args}To{$ReturnOnly}{$TypeArgsPass2} put_{$a2}_{$r2}("
                    + "String fname{$ObjectClassArgs2}IFunc{$Args}To{$ReturnOnly}{$TypeArgsPass2} func, {$StringFunction} stringFunction) {";
                method += "\n\t\treturn put_{$a}_{$r}(fname, {$ObjectClassArgsPass2}func, stringFunction);";
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
